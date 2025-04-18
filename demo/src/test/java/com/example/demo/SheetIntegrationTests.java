package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.Sheet;
import com.example.demo.model.Book;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SheetIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeAll
    void setUpOnce() {
        testBook = new Book();
        testBook.setName("Test Book");
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testCreateSheet() throws Exception {
        Sheet sheet = new Sheet();
        sheet.setName("Test Sheet 1");
        sheet.setBook(testBook);

        mockMvc.perform(post("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isCreated()) // Assuming controller returns 201
                .andExpect(jsonPath("$.data.name").value("Test Sheet 1"))
                .andExpect(jsonPath("$.data.id").exists());
    }
}
