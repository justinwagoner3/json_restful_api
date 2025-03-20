package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.model.Sheet;
import com.example.demo.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    // Overloaded method for Sheet operations (no rowNum, colNum, value, formula)
    public void logActivity(Sheet sheet, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
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
