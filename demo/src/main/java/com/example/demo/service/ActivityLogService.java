package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.model.Book;
import com.example.demo.model.Sheet;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.SheetRepository;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final SheetRepository sheetRepository;
    private final BookRepository bookRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, SheetRepository sheetRepository, BookRepository bookRepository) {
        this.activityLogRepository = activityLogRepository;
        this.sheetRepository = sheetRepository;
        this.bookRepository = bookRepository;
    }

    // Method for Book operations (no sheet, rowNum, colNum, value, formula)
    public void logActivityBook(Integer bookId, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        // Fetch Book object before logging
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with ID " + bookId + " not found, cannot log activity."));
        logActivity(bookId, null, null, null, book.getName(), null, updatedBy, operation, entityType);
    }

    // Method for Sheet operations (no rowNum, colNum, value, formula)
    public void logActivitySheet(Integer sheetId, String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        // Fetch Sheet object before logging
        Sheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new IllegalArgumentException("Sheet with ID " + sheetId + " not found, cannot log activity."));

        logActivity(sheet.getBook().getId(), sheetId, null, null, sheet.getName(), null, updatedBy, operation, entityType);
    }

    // Full method
    public void logActivity(Integer bookId, Integer sheetId, Integer rowNum, String colNum, String value, String formula,
                            String updatedBy, ActivityLog.OperationType operation, ActivityLog.EntityType entityType) {
        ActivityLog log = new ActivityLog();
        log.setSheetId(sheetId);
        log.setBookId(bookId);
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
