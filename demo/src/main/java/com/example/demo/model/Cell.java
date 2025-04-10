package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sheet_id", nullable = false)
    private Sheet sheet;

    @Column(name = "row_num", nullable = false)
    private Integer rowNum;

    @Column(name = "col_num", nullable = false, length = 10)
    private String colNum;

    @Lob
    private String value;

    @Lob
    private String formula;

    public Cell(Sheet sheet, Integer rowNum, String colNum, String value, String formula) {
        this.sheet = sheet;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.value = value;
        this.formula = formula;
    }
}
