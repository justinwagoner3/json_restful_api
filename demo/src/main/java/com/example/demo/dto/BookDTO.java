package com.example.demo.dto;

import com.example.demo.model.Book;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDTO {
    private Integer id;
    private String name;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.name = book.getName();
    }
}
