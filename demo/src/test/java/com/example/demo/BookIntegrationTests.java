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
        assertEquals(1, logs.size());
        assertEquals("Created Book", logs.get(0).getValue());
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
        assertEquals(1, logs.size());
        assertEquals("Updated Book", logs.get(0).getValue());
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/books/" + testBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book deleted successfully"));

        List<ActivityLog> logs = activityLogRepository.findByEntityTypeAndOperation(EntityType.BOOK, OperationType.DELETE);
        assertEquals(1, logs.size());
        assertEquals("Initial Book", logs.get(0).getValue());
    }
}
