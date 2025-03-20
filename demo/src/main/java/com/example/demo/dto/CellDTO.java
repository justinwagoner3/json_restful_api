package com.example.demo.dto;

import com.example.demo.model.Cell;

public class CellDTO {
    private Integer id;
    private Integer sheetId;
    private Integer rowNum;
    private String colNum;
    private String value;
    private String formula;

    public CellDTO(Cell cell) {
        this.id = cell.getId();
        this.sheetId = cell.getSheet().getId();
        this.rowNum = cell.getRowNum();
        this.colNum = cell.getColNum();
        this.value = cell.getValue();
        this.formula = cell.getFormula();
    }

    public Integer getId() { return id; }
    public Integer getSheetId() { return sheetId; }
    public Integer getRowNum() { return rowNum; }
    public String getColNum() { return colNum; }
    public String getValue() { return value; }
    public String getFormula() { return formula; }
}
