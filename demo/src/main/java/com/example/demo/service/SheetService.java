package com.example.demo.service;

import com.example.demo.exception.SheetNotFoundException; 
import com.example.demo.model.Sheet;
import com.example.demo.repository.SheetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SheetService {
    private final SheetRepository sheetRepository;

    public SheetService(SheetRepository sheetRepository) {
        this.sheetRepository = sheetRepository;
    }

    public List<Sheet> getAllSheets() {
        return sheetRepository.findAll();
    }

    public Sheet getSheetById(int id) {
        return sheetRepository.findById(id)
            .orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot GET."));
    }

    public Sheet createSheet(Sheet sheet) {
        if (sheet.getName() == null || sheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }
        return sheetRepository.save(sheet);
    }

    public Sheet updateSheet(int id, Sheet newSheet) {
        if (newSheet.getName() == null || newSheet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Sheet name is required and cannot be empty.");
        }
        return sheetRepository.findById(id).map(sheet -> {
            sheet.setName(newSheet.getName());
            return sheetRepository.save(sheet);
        }).orElseThrow(() -> new SheetNotFoundException("Sheet with ID " + id + " not found, cannot UPDATE."));
    }

    public void deleteSheet(int id) {
        if (!sheetRepository.existsById(id)) {
            throw new SheetNotFoundException("Sheet with ID " + id + " not found, cannot DELETE.");
        }
        sheetRepository.deleteById(id);
    }
}
