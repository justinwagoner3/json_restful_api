package com.example.demo.controller;

import com.example.demo.dto.CellDTO;
import com.example.demo.exception.CellNotFoundException;
import com.example.demo.exception.SheetNotFoundException;
import com.example.demo.model.Cell;
import com.example.demo.model.Sheet;
import com.example.demo.service.CellService;
import com.example.demo.service.SheetService;
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
    private final SheetService sheetService;

    public CellController(CellService cellService, SheetService sheetService) {
        this.cellService = cellService;
        this.sheetService = sheetService;
    }

    @GetMapping
    public ResponseEntity<List<CellDTO>> getCellsBySheetId(@RequestParam Integer sheetId) {
        Sheet sheet = sheetService.getSheetById(sheetId)
                .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
        List<CellDTO> cellDTOs = cellService.getCellsBySheet(sheet).stream().map(CellDTO::new).toList();
        return ResponseEntity.ok(cellDTOs);
    }

    @GetMapping("/{sheetId}/{rowNum}/{colNum}")
    public ResponseEntity<Object> getCellBySheetRowCol(@PathVariable int sheetId, @PathVariable int rowNum, @PathVariable String colNum) {
        Sheet sheet = sheetService.getSheetById(sheetId)
                .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
        Optional<Cell> cell = cellService.getCellBySheetRowCol(sheet, rowNum, colNum);

        return cell.map(c -> ResponseEntity.ok((Object) new CellDTO(c))) 
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Cell not found for Sheet ID " + sheetId + ", Row " + rowNum + ", Column " + colNum,
                    "path", "/cells/" + sheetId + "/" + rowNum + "/" + colNum)));
    }

    @PostMapping
    public ResponseEntity<Object> createOrUpdateCell(@RequestBody Map<String, Object> requestBody) {
        try {
            Integer sheetId = (Integer) requestBody.get("sheetId");
            if (sheetId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", "Sheet ID is required.", "path", "/cells"));
            }
            Sheet sheet = sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));

            Integer rowNum = (Integer) requestBody.get("rowNum");
            String colNum = (String) requestBody.get("colNum");
            String value = (String) requestBody.getOrDefault("value", null);
            String formula = (String) requestBody.getOrDefault("formula", null);

            if (rowNum == null || colNum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", "Row number and column number are required.", "path", "/cells"));
            }

            Cell cell = new Cell();
            cell.setSheet(sheet);
            cell.setRowNum(rowNum);
            cell.setColNum(colNum);
            cell.setValue(value);
            cell.setFormula(formula);

            Cell createdCell = cellService.createOrUpdateCell(cell);
            return ResponseEntity.ok(new CellDTO(createdCell));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", e.getMessage(), "path", "/cells"));
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/cells"));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateCell(@RequestBody Map<String, Object> requestBody) {
        return createOrUpdateCell(requestBody); // Calls the same logic as POST
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteCell(@RequestParam Integer sheetId, @RequestParam Integer rowNum, @RequestParam String colNum) {
        try {
            Sheet sheet = sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
            cellService.deleteCell(sheet, rowNum, colNum);
            return ResponseEntity.noContent().build();
        } catch (CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/cells"));
        }
    }
}