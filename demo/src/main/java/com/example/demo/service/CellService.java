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

    public CellService(CellRepository cellRepository, SheetRepository sheetRepository, ActivityLogRepository activityLogRepository) {
        this.cellRepository = cellRepository;
        this.sheetRepository = sheetRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public List<Cell> getCellsBySheetId(int sheetId) {
        return cellRepository.findBySheetId(sheetId);
    }

    public Optional<Cell> getCellBySheetRowCol(int sheetId, int rowNum, String colNum) {
        return cellRepository.findBySheetIdAndRowNumAndColNum(sheetId, rowNum, colNum);
    }

    @Transactional
    public Cell createOrUpdateCell(Cell cell) {
        if (cell.getSheet() == null || cell.getSheet().getId() == null || cell.getRowNum() == null || cell.getColNum() == null) {
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

            activityLogRepository.save(new ActivityLog(sheet, cell.getRowNum(), cell.getColNum(), cell.getValue(),
                    cell.getFormula(), "SYSTEM", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.CELL));
            return cellRepository.save(updatedCell);
        }

        // New cell creation
        activityLogRepository.save(new ActivityLog(sheet, cell.getRowNum(), cell.getColNum(), cell.getValue(),
                cell.getFormula(), "SYSTEM", ActivityLog.OperationType.ADD, ActivityLog.EntityType.CELL));
        return cellRepository.save(cell);
    }

    @Transactional
    public void deleteCell(int sheetId, int rowNum, String colNum) {
        Optional<Cell> cell = cellRepository.findBySheetIdAndRowNumAndColNum(sheetId, rowNum, colNum);

        if (cell.isPresent()) {
            activityLogRepository.save(new ActivityLog(cell.get().getSheet(), rowNum, colNum, cell.get().getValue(),
                    cell.get().getFormula(), "SYSTEM", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.CELL));
            cellRepository.delete(cell.get());
        } else {
            throw new CellNotFoundException("Cell not found for Sheet ID " + sheetId + ", Row " + rowNum + ", Column " + colNum);
        }
    }
}
