package com.example.demo.service;

import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.Sheet;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.SheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class SheetService {
    private final SheetRepository sheetRepository;
    private final ActivityLogService activityLogService;

    public SheetService(SheetRepository sheetRepository, ActivityLogService activityLogService) {
        this.sheetRepository = sheetRepository;
        this.activityLogService = activityLogService;
    }

    public List<Sheet> getAllSheets() {
        return sheetRepository.findAll();
    }

    public Optional<Sheet> getSheetById(int id) {
        return sheetRepository.findById(id);
    }

    public Optional<Sheet> getSheetByName(String name) {
        return sheetRepository.findByName(name);
    }

    public Sheet createSheet(Sheet sheet) {
        if (sheet.getName() == null || sheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }

        try {
            Sheet createdSheet = sheetRepository.save(sheet);

            // Log the creation with entityType = SHEET
            activityLogService.logActivity(createdSheet, "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.SHEET);

            return createdSheet;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sheet name already exists.");
        }
    }

    public Sheet updateSheet(int id, Sheet newSheet) {
        if (newSheet.getName() == null || newSheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }
        return sheetRepository.findById(id).map(sheet -> {
            sheet.setName(newSheet.getName());
            Sheet updatedSheet = sheetRepository.save(sheet);

            // Log the update with entityType = SHEET
            activityLogService.logActivity(updatedSheet, "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.SHEET);

            return updatedSheet;
        }).orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot UPDATE."));
    }

    public void deleteSheet(int id) {
        Sheet sheet = sheetRepository.findById(id)
            .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot DELETE."));

        sheetRepository.deleteById(id);

        // Log the deletion with entityType = SHEET
        activityLogService.logActivity(sheet, "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.SHEET);
    }
}
