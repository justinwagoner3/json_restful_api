package com.example.demo.service;

import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.Sheet;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.SheetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SheetService {
    private final SheetRepository sheetRepository;
    private final ActivityLogRepository activityLogRepository;

    public SheetService(SheetRepository sheetRepository, ActivityLogRepository activityLogRepository) {
        this.sheetRepository = sheetRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public List<Sheet> getAllSheets() {
        return sheetRepository.findAll();
    }

    public Sheet getSheetById(int id) {
        return sheetRepository.findById(id)
            .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot GET."));
    }

    public Sheet createSheet(Sheet sheet) {
        if (sheet.getName() == null || sheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }
        Sheet createdSheet = sheetRepository.save(sheet);

        // Log the creation with entityType = SHEET
        logActivity(createdSheet, "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.SHEET);

        return createdSheet;
    }

    public Sheet updateSheet(int id, Sheet newSheet) {
        if (newSheet.getName() == null || newSheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }
        return sheetRepository.findById(id).map(sheet -> {
            sheet.setName(newSheet.getName());
            Sheet updatedSheet = sheetRepository.save(sheet);

            // Log the update with entityType = SHEET
            logActivity(updatedSheet, "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.SHEET);

            return updatedSheet;
        }).orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot UPDATE."));
    }

    public void deleteSheet(int id) {
        Sheet sheet = sheetRepository.findById(id)
            .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot DELETE."));

        sheetRepository.deleteById(id);

        // Log the deletion with entityType = SHEET
        logActivity(sheet, "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.SHEET);
    }

    private void logActivity(Sheet sheet, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        ActivityLog log = new ActivityLog();
        log.setSheet(sheet);
        log.setRowNum(null); // Since it's a Sheet operation
        log.setColNum(null); // Not applicable for sheets
        log.setValue(sheet.getName());
        log.setFormula(null);
        log.setUpdatedBy(updatedBy);
        log.setOperation(operation);
        log.setEntityType(entityType);
        activityLogRepository.save(log);
    }
}
