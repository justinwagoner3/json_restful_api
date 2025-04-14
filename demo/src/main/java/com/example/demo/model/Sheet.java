package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(
    name = "sheets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "sheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Cell> cells;
}
