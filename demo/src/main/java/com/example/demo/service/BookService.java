package com.example.demo.service;

import com.example.demo.exception.*;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;
import com.example.demo.model.ActivityLog;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final ActivityLogService activityLogService;

    public BookService(BookRepository bookRepository, ActivityLogService activityLogService) {
        this.bookRepository = bookRepository;
        this.activityLogService = activityLogService;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(int id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> getBookByName(String name) {
        return bookRepository.findByName(name);
    }

    public Book createBook(Book book) {
        if (book.getName() == null || book.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Book name is required and cannot be empty.");
        }
        try {
            Book createdBook = bookRepository.save(book);
            activityLogService.logActivityBook(createdBook.getId(), "system", ActivityLog.OperationType.ADD, ActivityLog.EntityType.BOOK);
            return createdBook;
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book name already exists.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during sheet creation.");
        }
    }

    public Book updateBook(Integer id, Book updatedBook) {
        if (updatedBook.getName() == null || updatedBook.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Book name is required and cannot be empty.");
        }
        return bookRepository.findById(id)
                .map(book -> {
                    book.setName(updatedBook.getName());
                    Book retBook = bookRepository.save(book);
                    activityLogService.logActivityBook(id, "system", ActivityLog.OperationType.UPDATE, ActivityLog.EntityType.BOOK);
                    return retBook;
                })
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found."));
    }

    public void deleteBook(int id) {
        if (!bookRepository.existsById(id)) {
            throw new SheetNotFoundException("Book with ID " + id + " not found.");
        }
        activityLogService.logActivityBook(id, "system", ActivityLog.OperationType.DELETE, ActivityLog.EntityType.BOOK);
        bookRepository.deleteById(id);
    }
}
