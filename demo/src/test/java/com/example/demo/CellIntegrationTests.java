package com.example.demo;

import com.example.demo.model.Book;
import com.example.demo.model.Sheet;
import com.example.demo.model.Cell;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.ActivityLog.EntityType;
import com.example.demo.model.ActivityLog.OperationType;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.SheetRepository;
import com.example.demo.repository.CellRepository;
import com.example.demo.repository.ActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CellIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookRepository bookRepository;
    @Autowired private SheetRepository sheetRepository;
    @Autowired private CellRepository cellRepository;
    @Autowired private ActivityLogRepository activityLogRepository;

    private Book book;
    private Sheet sheet;

    @BeforeAll
    void setup() {
        book = new Book();
        book.setName("Test Book");
        book = bookRepository.save(book);

        sheet = new Sheet();
        sheet.setName("Test Sheet");
        sheet.setBook(book);
        sheet = sheetRepository.save(sheet);
    }

    @Test
    void testCreateCell() throws Exception {
        Map<String, Object> requestBody = Map.of(
            "sheet", Map.of("id", sheet.getId()),
            "rowNum", 1,
            "colNum", "A",
            "value", "123"
        );

        mockMvc.perform(post("/cells")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.value").value("123"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.CELL, OperationType.ADD);
        assertEquals(1, logs.size());
        assertEquals("123", logs.get(0).getValue());
    }

    @Test
    void testUpdateCell() throws Exception {
        Cell cell = new Cell(sheet, 2, "B", "initial", null);
        cell = cellRepository.save(cell);

        Map<String, Object> requestBody = Map.of(
            "sheet", Map.of("id", sheet.getId()),
            "rowNum", 2,
            "colNum", "B",
            "value", "updated"
        );

        mockMvc.perform(put("/cells")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.value").value("updated"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.CELL, OperationType.UPDATE);
        assertEquals(1, logs.size());
        assertEquals("updated", logs.get(0).getValue());
    }

    @Test
    void testDeleteCellByCoordinates() throws Exception {
        Cell cell = new Cell(sheet, 3, "C", "toDelete", null);
        cell = cellRepository.save(cell);

        Map<String, Object> requestBody = Map.of(
            "sheet", Map.of("id", sheet.getId()),
            "rowNum", 3,
            "colNum", "C"
        );

        mockMvc.perform(delete("/cells")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Cell deleted successfully"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.CELL, OperationType.DELETE);
        assertEquals(1, logs.size());
        assertEquals("toDelete", logs.get(0).getValue());
    }

    @Test
    void testDeleteCellById() throws Exception {
        Cell cell = new Cell(sheet, 4, "D", "byId", null);
        cell = cellRepository.save(cell);

        mockMvc.perform(delete("/cells/" + cell.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Cell deleted successfully by ID"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.CELL, OperationType.DELETE);
        assertEquals(1, logs.size());
        assertEquals("byId", logs.get(0).getValue());
    }

    @Test
    void testCreateCellMissingSheet() throws Exception {
        Map<String, Object> requestBody = Map.of(
            "rowNum", 5,
            "colNum", "E",
            "value", "missing"
        );

        mockMvc.perform(post("/cells")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Sheet object is required."));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.CELL);
        assertEquals(0, logs.size());
    }

    @Test
    void testGetCellBySheetRowCol_NotFound() throws Exception {
        mockMvc.perform(get("/cells/" + sheet.getId() + "/99/Z"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Cell not found for Sheet ID " + sheet.getId() + ", Row 99, Column Z"));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.CELL);
        assertEquals(0, logs.size());
    }

    void testPutCreatesNewCellIfNotExists() throws Exception {
        Map<String, Object> requestBody = Map.of(
            "sheet", Map.of("id", sheet.getId()),
            "rowNum", 10,
            "colNum", "X",
            "value", "newViaPut"
        );
    
        mockMvc.perform(put("/cells")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.value").value("newViaPut"));
    
        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.CELL, OperationType.ADD);
        assertEquals(1, logs.size());
        assertEquals("newViaPut", logs.get(0).getValue());
    }
    
}
