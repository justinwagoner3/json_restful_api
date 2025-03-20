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

    @GetMapping
    public ResponseEntity<List<Cell>> getCellsBySheetId(@RequestParam Integer sheetId) {
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
    public ResponseEntity<Object> createOrUpdateCell(
            @RequestParam(value = "sheetId", required = false) Integer sheetId, 
            @RequestBody Map<String, Object> requestBody) {
        try {
            // Try to get sheetId from query param first, then fall back to JSON body
            if (sheetId == null && requestBody.containsKey("sheetId")) {
                sheetId = (Integer) requestBody.get("sheetId");
            }

            if (sheetId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Sheet ID is required.",
                        "path", "/cells"
                    ));
            }

            // Extract other fields
            Integer rowNum = (Integer) requestBody.get("rowNum");
            String colNum = (String) requestBody.get("colNum");
            String value = (String) requestBody.getOrDefault("value", null);
            String formula = (String) requestBody.getOrDefault("formula", null);

            if (rowNum == null || colNum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "status", 400,
                        "error", "Bad Request",
                        "message", "Row number and column number are required.",
                        "path", "/cells"
                    ));
            }

            Cell cell = new Cell();
            cell.setSheetId(sheetId);
            cell.setRowNum(rowNum);
            cell.setColNum(colNum);
            cell.setValue(value);
            cell.setFormula(formula);

            Cell createdCell = cellService.createOrUpdateCell(cell);
            return ResponseEntity.ok(createdCell);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", 400,
                            "error", "Bad Request",
                            "message", e.getMessage(),
                            "path", "/cells"
                    ));
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/cells"
                    ));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateCell(
            @RequestParam(value = "sheetId", required = false) Integer sheetId,
            @RequestBody Map<String, Object> requestBody) {
        return createOrUpdateCell(sheetId, requestBody);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteCell(
            @RequestParam(value = "sheetId", required = false) Integer sheetId,
            @RequestParam(value = "rowNum", required = false) Integer rowNum,
            @RequestParam(value = "colNum", required = false) String colNum,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            // Allow delete parameters to come from query or request body
            if (sheetId == null && requestBody != null && requestBody.containsKey("sheetId")) {
                sheetId = (Integer) requestBody.get("sheetId");
            }
            if (rowNum == null && requestBody != null && requestBody.containsKey("rowNum")) {
                rowNum = (Integer) requestBody.get("rowNum");
            }
            if (colNum == null && requestBody != null && requestBody.containsKey("colNum")) {
                colNum = (String) requestBody.get("colNum");
            }

            if (sheetId == null || rowNum == null || colNum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "status", 400,
                                "error", "Bad Request",
                                "message", "Sheet ID, row number, and column number are required for deletion.",
                                "path", "/cells"
                        ));
            }

            cellService.deleteCell(sheetId, rowNum, colNum);
            return ResponseEntity.noContent().build();
        } catch (CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/cells"
                    ));
        }
    }
}
