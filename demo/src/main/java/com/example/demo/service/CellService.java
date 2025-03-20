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
    private final ActivityLogService activityLogService;

    public CellService(CellRepository cellRepository, SheetRepository sheetRepository, ActivityLogService activityLogService) {
        this.cellRepository = cellRepository;
        this.sheetRepository = sheetRepository;
        this.activityLogService = activityLogService;
    }

    public List<Cell> getCellsBySheetId(int sheetId) {
        return cellRepository.findBySheetId(sheetId);
    }

    public Optional<Cell> getCellBySheetRowCol(int sheetId, int rowNum, String colNum) {
        return cellRepository.findBySheetIdAndRowNumAndColNum(sheetId, rowNum, colNum);
    }

    @Transactional
    public Cell createOrUpdateCell(Cell cell) {
        if (cell.getSheet() == null || cell.getSheet().getId() == null || cell.getRowNum() <= 0 || cell.getColNum() == null) {
            throw new IllegalArgumentException("Sheet ID, row number, and column number are required.");
        }

        // Ensure the sheet exists
        Sheet sheet = sheetRepository.findById(cell.getSheet().getId())
                .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + cell.getSheet().getId() + " not found."));

        // Check if cell exists
        Optional<Cell> existingCell = cellRepository.findBySheetIdAndRowNumAndColNum(sheet.getId(), cell.getRowNum(), cell.getColNum());

        if (existingCell.isPresent()) {
            Cell updatedCell = existingCell.get();
            updatedCell.setValue(cell.getValue());
            updatedCell.setFormula(cell.getFormula());

            if ((cell.getValue() == null || cell.getValue().trim().isEmpty()) &&
                (cell.getFormula() == null || cell.getFormula().trim().isEmpty())) {
                deleteCell(sheet.getId(), cell.getRowNum(), cell.getColNum());
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
    public void deleteCell(int sheetId, int rowNum, String colNum) {
        Optional<Cell> cell = cellRepository.findBySheetIdAndRowNumAndColNum(sheetId, rowNum, colNum);

        if (cell.isPresent()) {
            activityLogService.logActivity(cell.get().getSheet(), rowNum, colNum, cell.get().getValue(),
                    cell.get().getFormula(), "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.CELL);
            cellRepository.delete(cell.get());
        } else {
            throw new CellNotFoundException("Cell not found for Sheet ID " + sheetId + ", Row " + rowNum + ", Column " + colNum);
        }
    }
}
