package com.example.demo.controller;

import com.example.demo.dto.SheetDTO;
import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.Sheet;
import com.example.demo.model.Book;
import com.example.demo.service.SheetService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sheets")
public class SheetController {
    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @GetMapping
    public ResponseEntity<Object> getAllSheets() {
        List<SheetDTO> sheets = sheetService.getAllSheets()
                .stream()
                .map(SheetDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("status", 200, "data", sheets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getSheetById(@PathVariable int id) {
        try {
            Sheet sheet = sheetService.getSheetById(id)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found."));
            return ResponseEntity.ok(Map.of("status", 200, "data", new SheetDTO(sheet)));
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

    public static class CreateSheetRequest {
        public String name;
        public Book book;
    }

    @PostMapping
    public ResponseEntity<Object> createSheet(@RequestBody CreateSheetRequest request) {
        try {
            Sheet createdSheet = sheetService.createSheet(request.name, request.book);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", 201, "data", new SheetDTO(createdSheet)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", 400, "error", "Bad Request", "message", e.getMessage(), "path", "/sheets"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("status", e.getStatusCode().value(), "error", "Conflict", "message", e.getReason(), "path", "/sheets"));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSheet(@PathVariable int id, @RequestBody Sheet updatedSheet) {
        try {
            Sheet sheet = sheetService.updateSheet(id, updatedSheet);
            return ResponseEntity.ok(Map.of("status", 200, "data", new SheetDTO(sheet)));
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
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                            "status", e.getStatusCode().value(),
                            "error", "Conflict",
                            "message", e.getReason(),
                            "path", "/sheets/" + id
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteSheet(@PathVariable int id) {
        try {
            sheetService.deleteSheet(id);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Sheet deleted successfully"));
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
