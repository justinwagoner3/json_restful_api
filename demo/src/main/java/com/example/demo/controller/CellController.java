package com.example.demo.controller;

import com.example.demo.dto.CellDTO;
import com.example.demo.exception.*;
import com.example.demo.model.Cell;
import com.example.demo.model.Sheet;
import com.example.demo.service.CellService;
import com.example.demo.service.SheetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cells")
public class CellController {
    private final CellService cellService;
    private final SheetService sheetService;

    public CellController(CellService cellService, SheetService sheetService) {
        this.cellService = cellService;
        this.sheetService = sheetService;
    }

    private Sheet resolveSheet(Map<String, Object> sheetMap) {
        if (sheetMap == null) {
            throw new IllegalArgumentException("Sheet object is required.");
        }

        Integer sheetId = (Integer) sheetMap.get("id");
        String sheetName = (String) sheetMap.get("name");
        Map<String, Object> bookMap = (Map<String, Object>) sheetMap.get("book");

        if (sheetId != null) {
            return sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
        } else if (sheetName != null && bookMap != null) {
            Integer bookId = (Integer) bookMap.get("id");
            String bookName = (String) bookMap.get("name");

            if (bookId != null) {
                return sheetService.getSheetByBookIdAndName(bookId, sheetName)
                        .orElseThrow(() -> new SheetNotFoundException("Sheet with name '" + sheetName + "' not found under Book ID " + bookId));
            } else if (bookName != null) {
                return sheetService.getSheetByBookNameAndSheetName(bookName, sheetName)
                        .orElseThrow(() -> new SheetNotFoundException("Sheet with name '" + sheetName + "' not found under Book '" + bookName + "'"));
            } else {
                throw new IllegalArgumentException("Book must have an ID or name.");
            }
        } else {
            throw new IllegalArgumentException("Either sheetId or sheet name with book must be provided.");
        }
    }

    @GetMapping
    public ResponseEntity<Object> getCellsBySheetId(@RequestParam Integer sheetId) {
        try {
            Sheet sheet = sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
            List<CellDTO> cellDTOs = cellService.getCellsBySheet(sheet).stream().map(CellDTO::new).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("status", 200, "data", cellDTOs));
        } catch (SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/cells"));
        }
    }

    @GetMapping("/{sheetId}/{rowNum}/{colNum}")
    public ResponseEntity<Object> getCellBySheetRowCol(@PathVariable int sheetId, @PathVariable int rowNum, @PathVariable String colNum) {
        try {
            Sheet sheet = sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
            Cell cell = cellService.getCellBySheetRowCol(sheet, rowNum, colNum)
                    .orElseThrow(() -> new CellNotFoundException("Cell not found for Sheet ID " + sheetId + ", Row " + rowNum + ", Column " + colNum));
            return ResponseEntity.ok(Map.of("status", 200, "data", new CellDTO(cell)));
        } catch (SheetNotFoundException | CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", e.getMessage(),
                        "path", "/cells/" + sheetId + "/" + rowNum + "/" + colNum
                    ));
        }
    }

    @PostMapping
    public ResponseEntity<Object> createOrUpdateCell(@RequestBody Map<String, Object> requestBody) {
        try {
            Sheet sheet = resolveSheet((Map<String, Object>) requestBody.get("sheet"));
            Integer rowNum = (Integer) requestBody.get("rowNum");
            String colNum = (String) requestBody.get("colNum");
            String value = (String) requestBody.getOrDefault("value", null);
            String formula = (String) requestBody.getOrDefault("formula", null);

            if (rowNum == null || colNum == null) {
                throw new IllegalArgumentException("Row number and column number are required.");
            }

            Cell cell = new Cell(sheet, rowNum, colNum, value, formula);
            Cell createdCell = cellService.createOrUpdateCell(cell);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("status", 201, "data", new CellDTO(createdCell)));
        } catch (IllegalArgumentException | SheetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", e.getMessage(), "path", "/cells"));
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateCell(@RequestBody Map<String, Object> requestBody) {
        return createOrUpdateCell(requestBody);
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteCellHumanReadable(@RequestBody Map<String, Object> requestBody) {
        try {
            Sheet sheet = resolveSheet((Map<String, Object>) requestBody.get("sheet"));
            Integer rowNum = (Integer) requestBody.get("rowNum");
            String colNum = (String) requestBody.get("colNum");

            if (rowNum == null || colNum == null) {
                throw new IllegalArgumentException("Row number and column number are required.");
            }

            cellService.deleteCellByCoordinates(sheet, rowNum, colNum);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Cell deleted successfully"));
        } catch (IllegalArgumentException | SheetNotFoundException | CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/cells"));
        }
    }

    @DeleteMapping("/{cellId}")
    public ResponseEntity<Object> deleteCellById(@PathVariable Integer cellId) {
        try {
            cellService.deleteCellById(cellId);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Cell deleted successfully by ID"));
        } catch (CellNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "status", 404,
                        "error", "Not Found",
                        "message", e.getMessage(),
                        "path", "/cells/" + cellId
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", 500,
                        "error", "Internal Server Error",
                        "message", "Unexpected error occurred while deleting the cell.",
                        "path", "/cells/" + cellId
                    ));
        }
    }

    @DeleteMapping("/{sheetId}/{rowNum}/{colNum}")
    public ResponseEntity<Object> deleteCellBySheetRowCol(@PathVariable int sheetId,
                                                        @PathVariable int rowNum,
                                                        @PathVariable String colNum) {
        try {
            Sheet sheet = sheetService.getSheetById(sheetId)
                    .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + sheetId + " not found."));
            cellService.deleteCellByCoordinates(sheet, rowNum, colNum);
            return ResponseEntity.ok(Map.of("status", 200, "message", "Cell deleted successfully by sheet/row/col"));
        } catch (SheetNotFoundException | CellNotFoundException e) {
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
