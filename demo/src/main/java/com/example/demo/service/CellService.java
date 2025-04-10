package com.example.demo.service;

import com.example.demo.exception.CellNotFoundException;
import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.Cell;
import com.example.demo.model.Sheet;
import com.example.demo.model.ActivityLog;
import com.example.demo.repository.CellRepository;
import com.example.demo.repository.SheetRepository;
import com.example.demo.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import java.util.*;
import java.util.regex.*;

@Service
public class CellService {
    private final CellRepository cellRepository;
    private final SheetService sheetService;
    private final ActivityLogService activityLogService;

    // key: A1, value: set of dependent cells (like A3, A5)
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();

    public CellService(CellRepository cellRepository, SheetService sheetService, ActivityLogService activityLogService) {
        this.cellRepository = cellRepository;
        this.sheetService = sheetService;
        this.activityLogService = activityLogService;
    }

    public Cell createOrUpdateCell(Cell cell) {
        String cellKey = cellKey(cell);

        if (cell.getFormula() != null && cell.getFormula().startsWith("=")) {
            registerDependencies(cellKey, cell.getFormula());
            String computedValue = evaluateFormula(cell.getSheet(), cell.getFormula().substring(1));
            cell.setValue(computedValue);
        }

        Optional<Cell> existing = getCellBySheetRowCol(cell.getSheet(), cell.getRowNum(), cell.getColNum());

        Cell result;
        if (existing.isPresent()) {
            Cell toUpdate = existing.get();
            toUpdate.setValue(cell.getValue());
            toUpdate.setFormula(cell.getFormula());
            result = cellRepository.save(toUpdate);
            activityLogService.logActivity(cell.getSheet().getId(), cell.getRowNum(), cell.getColNum(), cell.getValue(), cell.getFormula(), "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.CELL);

        } else {
            result = cellRepository.save(cell);
            activityLogService.logActivity(cell.getSheet().getId(), cell.getRowNum(), cell.getColNum(), cell.getValue(), cell.getFormula(), "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.CELL);
        }

        recalculateDependents(cellKey, cell.getSheet());
        return result;
    }

    private void registerDependencies(String cellKey, String formula) {
        Set<String> refs = extractCellRefs(formula);
        for (String ref : refs) {
            dependencyGraph.computeIfAbsent(ref, k -> new HashSet<>()).add(cellKey);
        }
    }

    private Set<String> extractCellRefs(String formula) {
        Set<String> refs = new HashSet<>();
        Matcher matcher = Pattern.compile("[A-Z]+[0-9]+").matcher(formula);
        while (matcher.find()) {
            refs.add(matcher.group());
        }
        return refs;
    }

    private void recalculateDependents(String changedCellKey, Sheet sheet) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(changedCellKey);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> dependents = dependencyGraph.getOrDefault(current, Set.of());

            for (String dep : dependents) {
                if (!visited.add(dep)) continue;
                Cell depCell = getCell(sheet, dep);
                if (depCell != null && depCell.getFormula() != null) {
                    String newValue = evaluateFormula(sheet, depCell.getFormula().substring(1));
                    depCell.setValue(newValue);
                    cellRepository.save(depCell);
                }
                queue.add(dep);
            }
        }
    }

    private Cell getCell(Sheet sheet, String cellKey) {
        String col = cellKey.replaceAll("\\d", "");
        int row = Integer.parseInt(cellKey.replaceAll("\\D", ""));
        return getCellBySheetRowCol(sheet, row, col).orElse(null);
    }

    private String cellKey(Cell cell) {
        return cell.getColNum() + cell.getRowNum();
    }

    private String evaluateFormula(Sheet sheet, String expression) {
        String[] tokens = expression.split("(?=[+\\-*/])|(?<=[+\\-*/])");
        if (tokens.length != 3) throw new IllegalArgumentException("Only simple formulas like A1+A2 are supported.");

        String ref1 = tokens[0].trim();
        String operator = tokens[1].trim();
        String ref2 = tokens[2].trim();

        String col1 = ref1.replaceAll("\\d", "");
        int row1 = Integer.parseInt(ref1.replaceAll("\\D", ""));
        String col2 = ref2.replaceAll("\\d", "");
        int row2 = Integer.parseInt(ref2.replaceAll("\\D", ""));

        String val1 = getCellBySheetRowCol(sheet, row1, col1).map(Cell::getValue).orElse("0");
        String val2 = getCellBySheetRowCol(sheet, row2, col2).map(Cell::getValue).orElse("0");

        double num1 = Double.parseDouble(val1);
        double num2 = Double.parseDouble(val2);
        double result;

        switch (operator) {
            case "+": result = num1 + num2; break;
            case "-": result = num1 - num2; break;
            case "*": result = num1 * num2; break;
            case "/": result = (num2 != 0) ? num1 / num2 : 0; break;
            default: throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        return String.valueOf(result);
    }

    // Existing method you already have
    public Optional<Cell> getCellBySheetRowCol(Sheet sheet, int rowNum, String colNum) {
        return cellRepository.findBySheetAndRowNumAndColNum(sheet, rowNum, colNum);
    }

    public List<Cell> getCellsBySheet(Sheet sheet) {
        return cellRepository.findBySheet(sheet);
    }

    public void deleteCell(Sheet sheet, Integer rowNum, String colNum) {
        Cell cell = cellRepository.findBySheetAndRowNumAndColNum(sheet, rowNum, colNum)
            .orElseThrow(() -> new CellNotFoundException("Cell not found for deletion."));
        activityLogService.logActivity(sheet.getId(), rowNum, colNum, cell.getValue(), cell.getFormula(), "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.CELL);
        cellRepository.delete(cell);
    }

}
