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

    @Column(name = "sheet_id", insertable = false, updatable = false) // Prevents duplicate persistence
    private Integer sheetId; // We need this for API interactions

    @ManyToOne
    @JoinColumn(name = "sheet_id", nullable = false)
    private Sheet sheet;  // This must exist!

    @Column(nullable = false)
    private Integer rowNum;

    @Column(nullable = false, length = 10)
    private String colNum;

    @Lob
    private String value;

    @Lob
    private String formula;
}
