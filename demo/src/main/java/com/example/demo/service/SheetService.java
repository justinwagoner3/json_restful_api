package com.example.demo.service;

import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.Sheet;
import com.example.demo.model.Book;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.SheetRepository;
import com.example.demo.repository.BookRepository;
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
    private final BookRepository bookRepository;

    public SheetService(SheetRepository sheetRepository, ActivityLogService activityLogService, BookRepository bookRepository) {
        this.sheetRepository = sheetRepository;
        this.activityLogService = activityLogService;
        this.bookRepository = bookRepository;
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

    public Sheet createSheet(String sheetName, Book inputBook) {
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }

        if (inputBook == null) {
            throw new IllegalArgumentException("Sheet must be associated with a book.");
        }

        Book book = null;

        if (inputBook.getId() != null) {
            book = bookRepository.findById(inputBook.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with ID " + inputBook.getId() + " not found."));
        } else if (inputBook.getName() != null) {
            book = bookRepository.findByName(inputBook.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with name \"" + inputBook.getName() + "\" not found."));
        } else {
            throw new IllegalArgumentException("Book must contain either an ID or name.");
        }

        Sheet sheet = new Sheet();
        sheet.setName(sheetName);
        sheet.setBook(book);

        try {
            Sheet createdSheet = sheetRepository.save(sheet);
            activityLogService.logActivity(createdSheet.getId(), "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.SHEET);
            return createdSheet;
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace(); 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sheet name already exists.");
        } catch (Exception e) {
            System.err.println("Generic failure: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during sheet creation.");
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
            activityLogService.logActivity(updatedSheet.getId(), "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.SHEET);

            return updatedSheet;
        }).orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot UPDATE."));
    }

    public void deleteSheet(int id) {
        Sheet sheet = sheetRepository.findById(id)
            .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot DELETE."));

        // Log the deletion with entityType = SHEET
        activityLogService.logActivity(sheet.getId(), "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.SHEET);

        sheetRepository.deleteById(id);
    }
}
