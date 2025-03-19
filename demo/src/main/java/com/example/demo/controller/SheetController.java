package com.example.demo.controller;

import com.example.demo.model.Sheet;
import com.example.demo.service.SheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/sheets")
public class SheetController {
    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping
    public List<Sheet> getAllSheets() {
        return sheetService.getAllSheets();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getSheetById(@PathVariable int id) {
        Optional<Sheet> sheet = sheetService.getSheetById(id);
        return sheet.<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404, 
                    "error", "Not Found", 
                    "message", "Sheet with ID " + id + " not found, cannot GET",
                    "path", "/sheets/" + id)));
    }

    @PostMapping
    public ResponseEntity<Object> createSheet(@RequestBody Sheet sheet) {
        if (sheet.getName() == null || sheet.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Sheet name is required and cannot be empty",
                    "path", "/sheets"
                ));
        }
        Sheet createdSheet = sheetService.createSheet(sheet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSheet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSheet(@PathVariable int id, @RequestBody Sheet updatedSheet) {
        if (updatedSheet.getName() == null || updatedSheet.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", "Sheet name is required and cannot be empty",
                    "path", "/sheets/" + id
                ));
        }
        try {
            Sheet sheet = sheetService.updateSheet(id, updatedSheet);
            return ResponseEntity.ok(sheet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Sheet with ID " + id + " not found, cannot UPDATE",
                    "path", "/sheets/" + id
                ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSheet(@PathVariable int id) {
        sheetService.deleteSheet(id);
        return ResponseEntity.noContent().build();
    }
}
