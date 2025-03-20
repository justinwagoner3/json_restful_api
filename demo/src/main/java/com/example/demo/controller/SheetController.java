package com.example.demo.controller;

import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.Sheet;
import com.example.demo.service.SheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Map;

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
        try {
            Sheet sheet = sheetService.getSheetById(id)
                .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found."));
            return ResponseEntity.ok(sheet);
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", e.getMessage(),
                    "path", "/sheets/" + id
                ));
        }
    }

    @PostMapping
    public ResponseEntity<Object> createSheet(@RequestBody Sheet sheet) {
        try {
            Sheet createdSheet = sheetService.createSheet(sheet);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSheet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", e.getMessage(),
                    "path", "/sheets"
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSheet(@PathVariable int id, @RequestBody Sheet updatedSheet) {
        try {
            Sheet sheet = sheetService.updateSheet(id, updatedSheet);
            return ResponseEntity.ok(sheet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", e.getMessage(),
                    "path", "/sheets/" + id
                ));
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", e.getMessage(),
                    "path", "/sheets/" + id
                ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteSheet(@PathVariable int id) {
        try {
            sheetService.deleteSheet(id);
            return ResponseEntity.noContent().build();
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", e.getMessage(),
                    "path", "/sheets/" + id
                ));
        }
    }
}
