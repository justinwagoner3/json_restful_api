package com.example.demo.repository;

import com.example.demo.model.Cell;
import com.example.demo.model.Sheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Integer> {
    List<Cell> findBySheet(Sheet sheet);
    Optional<Cell> findBySheetAndRowNumAndColNum(Sheet sheet, int rowNum, String colNum);
}
