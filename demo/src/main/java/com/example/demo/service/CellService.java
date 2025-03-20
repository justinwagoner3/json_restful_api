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
        if (cell.getSheet() == null || cell.getRowNum() == null || cell.getColNum() == null) {
            throw new IllegalArgumentException("Sheet, row number, and column number are required.");
        }

        // Ensure the sheet exists
        Sheet sheet = sheetRepository.findById(cell.getSheet().getId())
                .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + cell.getSheet().getId() + " not found."));

        cell.setSheet(sheet); // Ensures the reference is valid

        // Check if cell exists
        Optional<Cell> existingCell = cellRepository.findBySheetAndRowNumAndColNum(sheet, cell.getRowNum(), cell.getColNum());

        if (existingCell.isPresent()) {
            Cell updatedCell = existingCell.get();
            updatedCell.setValue(cell.getValue());
            updatedCell.setFormula(cell.getFormula());

            if ((cell.getValue() == null || cell.getValue().trim().isEmpty()) &&
                (cell.getFormula() == null || cell.getFormula().trim().isEmpty())) {
                deleteCell(sheet, cell.getRowNum(), cell.getColNum());
                return null;
            }

            activityLogService.logActivity(sheet, cell.getRowNum(), cell.getColNum(), cell.getValue(),
                    cell.getFormula(), "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.CELL);
            return cellRepository.save(updatedCell);
        }

        // New cell creation
        activityLogService.logActivity(sheet, cell.getRowNum(), cell.getColNum(), cell.getValue(),
                cell.getFormula(), "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.CELL);
        return cellRepository.save(cell);
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