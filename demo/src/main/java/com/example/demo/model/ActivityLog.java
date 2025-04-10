package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sheet_id") // this maps to the DB column
    private Integer sheetId;

    private Integer rowNum; // Nullable since it's only used for cells
    private String colNum;  // Nullable since it's only used for cells

    @Lob // Ensures the column is TEXT in MySQL
    private String value;

    @Lob // Ensures the column is TEXT in MySQL
    private String formula;

    @Column(nullable = false, length = 255)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    public enum OperationType {
        ADD, UPDATE, DELETE
    }

    public enum EntityType {
        SHEET, CELL
    }
}
