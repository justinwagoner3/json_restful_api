package com.example.demo.dto;

import com.example.demo.model.Sheet;

public class SheetDTO {
    private Integer id;
    private String name;
	private Integer bookId;

    public SheetDTO(Sheet sheet) {
        this.id = sheet.getId();
        this.name = sheet.getName();
		this.bookId = sheet.getBook().getId();
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
}
