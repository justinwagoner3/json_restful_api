# Table of Contents

- [Overview](#overview)
- [Base URL](#base-url)
- [Resources](#resources)
  - [Sheet](#sheet)
  - [Cell](#cell)
  - [Activity Log](#activity-log)
- [Capabilities](#capabilities)
- [Common Status Codes](#common-status-codes)
- [Endpoints](#endpoints)
  - [Sheet Endpoints](#sheet-endpoints)
  - [Cell Endpoints](#cell-endpoints)
- [Complete Walkthrough](#complete-walkthrough)
  - [Sheets API Operations](#sheets-api-operations)
  - [Cells API Operations](#cells-api-operations)
  - [Summary of Features Demonstrated](#summary-of-features-demonstrated)

# Overview
This RESTful API provides CRUD operations for managing **Sheets** and **Cells**, similar to a basic spreadsheet application. Each **Sheet** acts as a container for **Cells**, which can hold raw values or formulas (e.g., `=A1 + B2`).

The API is built using **Spring Boot** and exposes endpoints for:
- Creating, reading, updating, and deleting Sheets
- Creating, reading, updating, and deleting Cells
- Evaluating formulas with dependency tracking

# Base URL
```
http://localhost:8080
```

# Resources

* Spring Web (for REST API)
* Spring Data JPA (for database interaction)
* MySQL Driver (for database)

## Sheet
Represents a spreadsheet. A sheet can have multiple cells.

| Field   | Type   | Description       |
|---------|--------|-------------------|
| id      | int    | Unique identifier |
| name    | string | Sheet name        |

## Cell
Represents a single cell in a spreadsheet grid (like A1, B2, etc.).

| Field     | Type   | Description                              |
|-----------|--------|------------------------------------------|
| id        | int    | Unique identifier                        |
| sheetId   | int    | Foreign key to the sheet                 |
| row       | int    | Row number (e.g., 1, 2)                  |
| col       | string | Column name (e.g., A, B)                 |
| value     | string | Stored value or result of formula        |
| formula   | string | Optional formula (e.g., `=A1+B1`)        |

## Activity Log

Represents a history of operations performed on sheets or cells. This table can be used for **auditing**, **version tracking**, or implementing **undo/redo** features.

| Field       | Type                          | Description                                                                 |
|-------------|-------------------------------|-----------------------------------------------------------------------------|
| id          | int                           | Auto-incremented unique identifier for the log entry                        |
| entity_type | enum('SHEET','CELL')          | Type of entity affected by the operation (`SHEET` or `CELL`)                |
| operation   | enum('ADD','UPDATE','DELETE') | The kind of action performed                                                |
| sheet_id    | int                           | ID of the sheet involved in the operation                                   |
| row_num     | int                           | Row number of the affected cell (null if entity is a sheet)                 |
| col_num     | varchar(10)                   | Column name of the affected cell (null if entity is a sheet)                |
| value       | text                          | Final value after the operation (e.g., raw input or calculated result)      |
| formula     | text                          | Formula associated with the cell, if any (null if not applicable)           |
| updated_by  | varchar(255)                  | Identifier of the user or system that made the change                       |
| updated_at  | timestamp                     | Timestamp of when the update occurred (auto-generated and auto-updated)     |

# Capabilities

- Create/read/update/delete Sheets and Cells
- Input and evaluate formulas (with reference tracking)
- Return structured JSON responses with status codes
- No authentication currently required
- No user-specific data segregation (multi-tenancy not implemented)

# Common Status Codes

| Status Code | Meaning                |
|-------------|------------------------|
| 200 OK      | Successful request     |
| 201 Created | Resource was created   |
| 400 Bad Request | Invalid input       |
| 404 Not Found | Sheet/Cell not found |
| 500 Internal Server Error | Server error |

# Endpoints

## Sheet Endpoints

### `GET /sheets`  
Returns all sheets

### `GET /sheets/{id}`  
  Returns sheet by ID

### `POST /sheets`  
  Creates a new sheet  
  **Body:**
  ```json
  { "name": "sheet1" }
  ```

### `PUT /sheets/{id}`  
  Updates sheet name  
  **Body:**
  ```json
  { "name": "new-name" }
  ```

### `DELETE /sheets/{id}`  
  Deletes a sheet by ID

## Cell Endpoints

### `GET /cells?sheetId=1`  
  Returns all cells in a sheet

### `GET /cells/{id}`  
  Returns cell by ID

### `POST /cells`  
  Creates a new cell  
  **Body:**
  ```json
  {
    "sheetId": 1,
    "row": 1,
    "col": "A",
    "value": "42",
    "formula": null
  }
  ```

### `PUT /cells`  
  Updates a cell’s value or formula  
  **Body:**
  ```json
  {
    "sheet": { "name": "sheet1-updated" },
    "rowNum": 2,
    "colNum": "A",
    "value": "5"
  }
  ```

### `DELETE /cells`  
  Deletes a cell
  **Body:**
  ```json
  {
    "sheet": { "name": "sheet1-updated" },
    "rowNum": 1,
    "colNum": "A"
  }
  ```

# Complete Walkthrough

This guide demonstrates how to interact with the REST API, showcasing CRUD operations for sheets and cells.

## Sheets API Operations

### Create Sheet 1 (`sheet1`)
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet1" }'
```
#### Expected Response (201 Created)
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "name": "sheet1"
  }
}
```

### Create Sheet 2 (`sheet2`)
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet2" }'
```
#### Expected Response (201 Created)
```json
{
  "status": 201,
  "data": {
    "id": 2,
    "name": "sheet2"
  }
}
```

### Update `sheet1` to `sheet1-updated`
```sh
curl -X PUT "http://localhost:8080/sheets/1" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet1-updated" }'
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "name": "sheet1-updated"
  }
}
```

### Get Sheet by ID (`sheet1-updated`)
```sh
curl -X GET "http://localhost:8080/sheets/1"
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "name": "sheet1-updated"
  }
}
```

### Delete `sheet2`
```sh
curl -X DELETE "http://localhost:8080/sheets/2"
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "message": "Sheet deleted successfully"
}
```

### Get All Sheets (Only `sheet1-updated` Should Exist)
```sh
curl -X GET "http://localhost:8080/sheets"
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "name": "sheet1-updated"
    }
  ]
}
```

## Cells API Operations

### Create Cells A1 and A2 in `sheet1-updated`
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 1,
           "colNum": "A",
           "value": "8"
         }'

curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 2,
           "colNum": "A",
           "value": "18"
         }'
```

### Create a Formula Cell `=A1+A2` in Row 3
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 3,
           "colNum": "A",
           "formula": "=A1+A2"
         }'
```
#### Expected Response (201 Created)
```json
{
  "status": 201,
  "data": {
    "id": 3,
    "sheetId": 1,
    "rowNum": 3,
    "colNum": "A",
    "value": "26.0",
    "formula": "=A1+A2"
  }
}
```

### Update A2 to a New Value (Triggers A3 to Update)
```sh
curl -X PUT "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 2,
           "colNum": "A",
           "value": "5"
         }'
```

### Verify that A3 Now Shows `13.0` (8 + 5)
```sh
curl -X GET "http://localhost:8080/cells/1/3/A"
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "data": {
    "id": 3,
    "sheetId": 1,
    "rowNum": 3,
    "colNum": "A",
    "value": "13.0",
    "formula": "=A1+A2"
  }
}
```

### Try Creating a Cell in a Non-Existent Sheet
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "nonexistent-sheet" },
           "rowNum": 2,
           "colNum": "B",
           "value": "Should Fail",
           "formula": ""
         }'
```
#### Expected Response (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sheet with name \"nonexistent-sheet\" not found.",
  "path": "/cells"
}
```

### Delete Cell from `sheet1-updated`
```sh
curl -X DELETE "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 1,
           "colNum": "A"
         }'
```
#### Expected Response (200 OK)
```json
{
  "status": 200,
  "message": "Cell deleted successfully"
}
```

## Summary of Features Demonstrated
* Creating, updating, retrieving, and deleting **sheets**.  
* Creating, updating, retrieving, and deleting **cells**.  
* Error handling for **nonexistent sheets**.  
* **Formula dependency tracking**: changes in A1/A2 update A3.  
* Final verification to confirm changes.  