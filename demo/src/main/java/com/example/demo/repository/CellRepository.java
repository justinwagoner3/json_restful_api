package com.example.demo.repository;

import com.example.demo.model.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Integer> {
    List<Cell> findBySheetId(int sheetId);
    Optional<Cell> findBySheetIdAndRowNumAndColNum(int sheetId, int rowNum, String colNum);
}
