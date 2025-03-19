package com.example.demo.controller;

import com.example.demo.model.Sheet;
import com.example.demo.service.SheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Sheet> getSheetById(@PathVariable int id) {
        Optional<Sheet> sheet = sheetService.getSheetById(id);
        return sheet.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sheet createSheet(@RequestBody Sheet sheet) {
        return sheetService.createSheet(sheet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sheet> updateSheet(@PathVariable int id, @RequestBody Sheet updatedSheet) {
        try {
            Sheet sheet = sheetService.updateSheet(id, updatedSheet);
            return ResponseEntity.ok(sheet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSheet(@PathVariable int id) {
        sheetService.deleteSheet(id);
        return ResponseEntity.noContent().build();
    }
}
