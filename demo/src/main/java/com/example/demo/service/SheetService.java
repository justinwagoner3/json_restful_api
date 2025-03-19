package com.example.demo.service;

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

    public Optional<Sheet> getSheetById(int id) {
        return sheetRepository.findById(id);
    }

    public Sheet createSheet(Sheet sheet) {
        return sheetRepository.save(sheet);
    }

    public Sheet updateSheet(int id, Sheet newSheet) {
        return sheetRepository.findById(id).map(sheet -> {
            sheet.setName(newSheet.getName());
            return sheetRepository.save(sheet);
        }).orElseThrow(() -> new RuntimeException("Sheet not found"));
    }

    public void deleteSheet(int id) {
        sheetRepository.deleteById(id);
    }
}
