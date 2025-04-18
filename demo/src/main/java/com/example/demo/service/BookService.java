package com.example.demo.service;

import com.example.demo.exception.*;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
        return bookRepository.save(book);
    }

    public Book updateBook(Integer id, Book updatedBook) {
        if (updatedBook.getName() == null || updatedBook.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Book name is required and cannot be empty.");
        }
        return bookRepository.findById(id)
                .map(book -> {
                    book.setName(updatedBook.getName());
                    return bookRepository.save(book);
                })
                .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found."));
    }

    public void deleteBook(int id) {
        if (!bookRepository.existsById(id)) {
            throw new SheetNotFoundException("Book with ID " + id + " not found.");
        }
        bookRepository.deleteById(id);
    }
}
