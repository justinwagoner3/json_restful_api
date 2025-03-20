package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.model.Sheet;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.SheetRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final SheetRepository sheetRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, SheetRepository sheetRepository) {
        this.activityLogRepository = activityLogRepository;
        this.sheetRepository = sheetRepository;
    }

    // Overloaded method for Sheet operations (no rowNum, colNum, value, formula)
    public void logActivity(Sheet sheet, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        logActivity(sheet, null, null, sheet.getName(), null, updatedBy, operation, entityType);
    }

    // Overloaded method for Sheet operations (no rowNum, colNum, value, formula)
    public void logActivity(Integer sheetId, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        // Fetch Sheet object before logging
        Sheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new IllegalArgumentException("Sheet with ID " + sheetId + " not found, cannot log activity."));

        logActivity(sheet, null, null, sheet.getName(), null, updatedBy, operation, entityType);
    }

    // Full method for Cell operations (or full Sheet details)
    public void logActivity(Sheet sheet, Integer rowNum, String colNum, String value, String formula,
                            String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        ActivityLog log = new ActivityLog();
        log.setSheet(sheet);
        log.setRowNum(rowNum);
        log.setColNum(colNum);
        log.setValue(value);
        log.setFormula(formula);
        log.setUpdatedBy(updatedBy);
        log.setOperation(operation);
        log.setEntityType(entityType);
        activityLogRepository.save(log);
    }
}
