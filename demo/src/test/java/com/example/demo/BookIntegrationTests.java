package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.Book;
import com.example.demo.model.ActivityLog;
import com.example.demo.model.ActivityLog.EntityType;
import com.example.demo.model.ActivityLog.OperationType;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ActivityLogRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private Book testBook;

    @BeforeAll
    void setup() {
        testBook = new Book();
        testBook.setName("Initial Book");
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testCreateBook() throws Exception {
        Book newBook = new Book();
        newBook.setName("Created Book");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Created Book"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.BOOK, OperationType.ADD);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> "Created Book".equals(log.getValue())));
    }

    @Test
    void testUpdateBook() throws Exception {
        testBook.setName("Updated Book");

        mockMvc.perform(put("/books/" + testBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Book"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.BOOK, OperationType.UPDATE);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> "Updated Book".equals(log.getValue())));
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/books/" + testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book deleted successfully"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.BOOK, OperationType.DELETE);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(log -> "Updated Book".equals(log.getValue()) || "Initial Book".equals(log.getValue())));
    }

    @Test
    void testCreateBookMissingName() throws Exception {
        Book newBook = new Book();
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testUpdateNonexistentBook() throws Exception {
        Book fakeBook = new Book();
        fakeBook.setId(9999);
        fakeBook.setName("Ghost Book");

        mockMvc.perform(put("/books/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fakeBook)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void testDeleteNonexistentBook() throws Exception {
        mockMvc.perform(delete("/books/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with ID 9999 not found."))
                .andExpect(jsonPath("$.path").value("/books/9999"));
    }
    
    @Test
    void testDuplicateBookName() throws Exception {
        Book duplicate = new Book();
        duplicate.setName("Initial Book");

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
