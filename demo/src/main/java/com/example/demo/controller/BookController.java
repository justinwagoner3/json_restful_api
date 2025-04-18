package com.example.demo.controller;

import com.example.demo.dto.BookDTO;
import com.example.demo.exception.*;
import com.example.demo.model.Book;
import com.example.demo.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
public class BookController {
	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	public ResponseEntity<Object> getAllBooks() {
		List<BookDTO> books = bookService.getAllBooks()
				.stream()
				.map(BookDTO::new)
				.collect(Collectors.toList());
		return ResponseEntity.ok(Map.of("status", 200, "data", books));
	}

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable Integer id) {
        try {
            Book book = bookService.getBookById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found."));
            return ResponseEntity.ok(Map.of("status", 200, "data", new BookDTO(book)));
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/books/" + id
                    ));
        }
    }

	@PostMapping
	public ResponseEntity<Object> createBook(@RequestBody Book book) {
		try {
			Book createdBook = bookService.createBook(book);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("status", 201, "data", new BookDTO(createdBook)));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "error", "Bad Request", "message", e.getMessage(), "path", "/books"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("status", 409, "error", "Conflict", "message", e.getMessage(), "path", "/books"));
		}
	}

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateBook(@PathVariable Integer id, @RequestBody Book updatedBook) {
        try {
            Book book = bookService.updateBook(id, updatedBook);
            return ResponseEntity.ok(Map.of("status", 200, "data", new BookDTO(book)));
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/books/" + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", 400, "error", "Bad Request", "message", e.getMessage(), "path", "/books/" + id));
        }
    }

	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteBook(@PathVariable Integer id) {
		try {
			bookService.deleteBook(id);
			return ResponseEntity.ok(Map.of("status", 200, "message", "Book deleted successfully"));
		} catch (BookNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", 404, "error", "Not Found", "message", e.getMessage(), "path", "/books/" + id));
		}
	}
}
