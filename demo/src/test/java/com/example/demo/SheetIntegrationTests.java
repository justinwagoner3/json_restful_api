package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.Sheet;
import com.example.demo.model.Book;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class SheetIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private Book testBook;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        testBook = new Book();
        testBook.setName("IntegrationTestBook");
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testCreateSheet() throws Exception {
        
        Sheet sheet = new Sheet();
        sheet.setName("My Integration Sheet");
        sheet.setBook(testBook); // attach existing book

        mockMvc.perform(post("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sheet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Integration Sheet"))
                .andExpect(jsonPath("$.id").exists());

        //assertTrue(true);
    }
}
