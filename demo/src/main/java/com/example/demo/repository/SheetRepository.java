package com.example.demo.repository;

import com.example.demo.model.Sheet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SheetRepository extends JpaRepository<Sheet, Integer> {
    Optional<Sheet> findByName(String name);
}
