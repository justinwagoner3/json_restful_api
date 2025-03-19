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
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sheet_id", nullable = false)
    private Sheet sheet;

    private int rowNum;
    private String colNum;
    private String value;
    private String formula;  // Null if static value
}
