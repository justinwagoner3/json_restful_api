package com.example.demo.controller;

import com.example.demo.exception.CellNotFoundException;
import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.Cell;
import com.example.demo.service.CellService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/cells")
public class CellController {
    private final CellService cellService;

    public CellController(CellService cellService) {
        this.cellService = cellService;
    }

    @GetMapping("/{sheetId}")
    public ResponseEntity<List<Cell>> getCellsBySheetId(@PathVariable int sheetId) {
        return ResponseEntity.ok(cellService.getCellsBySheetId(sheetId));
    }

    @GetMapping("/{sheetId}/{rowNum}/{colNum}")
    public ResponseEntity<Object> getCellBySheetRowCol(@PathVariable int sheetId, @PathVariable int rowNum, @PathVariable String colNum) {
        Optional<Cell> cell = cellService.getCellBySheetRowCol(sheetId, rowNum, colNum);
        return cell.<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Cell not found for Sheet ID " + sheetId + ", Row " + rowNum + ", Column " + colNum,
                    "path", "/cells/" + sheetId + "/" + rowNum + "/" + colNum)));
    }

    @PostMapping
    public ResponseEntity<Object> createOrUpdateCell(@RequestBody Cell cell) {
        try {
            Cell savedCell = cellService.createOrUpdateCell(cell);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCell);
        } catch (IllegalArgumentException | SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", 400,
                    "error", "Bad Request",
                    "message", e.getMessage(),
                    "path", "/cells"
                ));
        }
    }

    @DeleteMapping("/{sheetId}/{rowNum}/{colNum}")
    public ResponseEntity<Object> deleteCell(@PathVariable int sheetId, @PathVariable int rowNum, @PathVariable String colNum) {
        try {
            cellService.deleteCell(sheetId, rowNum, colNum);
            return ResponseEntity.noContent().build();
        } catch (CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", e.getMessage(),
                    "path", "/cells/" + sheetId + "/" + rowNum + "/" + colNum
                ));
        }
    }
}
