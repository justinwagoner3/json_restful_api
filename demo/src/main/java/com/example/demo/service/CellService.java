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

@Service
public class CellService {
    private final CellRepository cellRepository;
    private final SheetRepository sheetRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogService activityLogService;

    public CellService(CellRepository cellRepository, SheetRepository sheetRepository, ActivityLogRepository activityLogRepository, ActivityLogService activityLogService) {
        this.cellRepository = cellRepository;
        this.sheetRepository = sheetRepository;
        this.activityLogRepository = activityLogRepository;
        this.activityLogService = activityLogService;
    }

    public List<Cell> getCellsBySheet(Sheet sheet) {
        return cellRepository.findBySheet(sheet);
    }

    public Optional<Cell> getCellBySheetRowCol(Sheet sheet, int rowNum, String colNum) {
        return cellRepository.findBySheetAndRowNumAndColNum(sheet, rowNum, colNum);
    }

    @Transactional
    public Cell createOrUpdateCell(Cell cell) {
        // Check for formula and compute value if needed
        if (cell.getFormula() != null && cell.getFormula().startsWith("=")) {
            String computedValue = evaluateFormula(cell.getSheet(), cell.getFormula().substring(1));
            cell.setValue(computedValue);
        }

        Optional<Cell> existing = getCellBySheetRowCol(cell.getSheet(), cell.getRowNum(), cell.getColNum());

        if (existing.isPresent()) {
            Cell toUpdate = existing.get();
            toUpdate.setValue(cell.getValue());
            toUpdate.setFormula(cell.getFormula());
            return cellRepository.save(toUpdate);
        } else {
            return cellRepository.save(cell);
        }
    }

    private String evaluateFormula(Sheet sheet, String expression) {
        // Only support "A1 + A2" for now
        String[] tokens = expression.split("(?=[+\\-*/])|(?<=[+\\-*/])");

        if (tokens.length != 3)
            throw new IllegalArgumentException("Only simple binary operations like A1+A2 are supported.");

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

    @Transactional
    public void deleteCell(Sheet sheet, int rowNum, String colNum) {
        Optional<Cell> cell = cellRepository.findBySheetAndRowNumAndColNum(sheet, rowNum, colNum);

        if (cell.isPresent()) {
            activityLogService.logActivity(sheet, rowNum, colNum, cell.get().getValue(),
                    cell.get().getFormula(), "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.CELL);
            cellRepository.delete(cell.get());
        } else {
            throw new CellNotFoundException("Cell not found for Sheet ID " + sheet.getId() + ", Row " + rowNum + ", Column " + colNum);
        }
    }
}