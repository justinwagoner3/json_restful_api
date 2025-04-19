package com.example.demo;

import com.example.demo.model.Book;
import com.example.demo.model.Sheet;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.ActivityLog.EntityType;
import com.example.demo.model.ActivityLog.OperationType;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.SheetRepository;
import com.example.demo.repository.ActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SheetIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BookRepository bookRepository;
    @Autowired private SheetRepository sheetRepository;
    @Autowired private ActivityLogRepository activityLogRepository;

    private Book testBook;

    @BeforeAll
    void setup() {
        testBook = new Book();
        testBook.setName("book1");
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testCreateSheetByBookId() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("Sheet A");
        sheet.setBook(testBook);

        mockMvc.perform(post("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Sheet A"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.SHEET, OperationType.ADD);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testCreateSheetMissingName() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setBook(testBook);

        mockMvc.perform(post("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Sheet name is required")));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.SHEET);
        assertTrue(logs.stream().noneMatch(log -> log.getOperation() == OperationType.ADD));
    }

    @Test
    void testGetAllSheets() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("Get All Test");
        sheet.setBook(testBook);
        sheetRepository.save(sheet);

        mockMvc.perform(get("/sheets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())));
    }

    @Test
    void testUpdateSheetByPath() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("To Update");
        sheet.setBook(testBook);
        sheet = sheetRepository.save(sheet);

        sheet.setName("Updated Sheet");
        mockMvc.perform(put("/sheets/" + sheet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Sheet"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.SHEET, OperationType.UPDATE);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testUpdateSheetByBody_MissingId() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("Missing ID");
        sheet.setBook(testBook);

        mockMvc.perform(put("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Sheet ID must be provided")));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.SHEET);
        assertTrue(logs.stream().noneMatch(log -> log.getOperation() == OperationType.UPDATE));
    }

    @Test
    void testDeleteSheetById() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("To Delete By ID");
        sheet.setBook(testBook);
        sheet = sheetRepository.save(sheet);

        mockMvc.perform(delete("/sheets/" + sheet.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sheet deleted successfully"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.SHEET, OperationType.DELETE);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testDeleteSheetByBody() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("To Delete By Body");
        sheet.setBook(testBook);
        sheet = sheetRepository.save(sheet);

        mockMvc.perform(delete("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sheet deleted successfully"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.SHEET, OperationType.DELETE);
        assertFalse(logs.isEmpty());
    }

    @Test
    void testDeleteSheetByBodyMissingBook() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("Missing Book");

        mockMvc.perform(delete("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Sheet name and book are required")));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.SHEET);
        assertTrue(logs.stream().noneMatch(log -> log.getOperation() == OperationType.DELETE));
    }

    @Test
    void testGetSheetByInvalidId() throws Exception {
        mockMvc.perform(get("/sheets/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void testDeleteSheetByInvalidId() throws Exception {
        mockMvc.perform(delete("/sheets/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));

        List<ActivityLog> logs = activityLogRepository.findByEntityType(EntityType.SHEET);
        assertTrue(logs.stream().noneMatch(log -> log.getOperation() == OperationType.DELETE));
    }
}
