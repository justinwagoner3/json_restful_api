# Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run with Docker](#run-with-docker)
  - [Database Connection](#database-connection)
- [Tech Stack](#tech-stack)
- [Domain Models](#domain-models)
  - [Book](#book)
  - [Sheet](#sheet)
  - [Cell](#cell)
  - [Activity Log](#activity-log)
- [Capabilities](#capabilities)
- [Common Status Codes](#common-status-codes)
- [API Endpoints](#api-endpoints)
  - [Book Endpoints](#book-endpoints)
  - [Sheet Endpoints](#sheet-endpoints)
  - [Cell Endpoints](#cell-endpoints)
- [Complete Walkthrough](#complete-walkthrough)
  - [Book API Operations](#book-api-operations)
  - [Sheet API Operations](#sheet-api-operations)
  - [Cells API Operations](#cells-api-operations)
  - [Database Results](#database-results)
- [Review and Retrospect](#review-and-retrospect)
- [Future Additions](#future-additions)


# Overview
This RESTful API provides CRUD operations for managing **Sheets** and **Cells**, similar to a basic spreadsheet application. Each **Sheet** acts as a container for **Cells**, which can hold raw values or formulas (e.g., `=A1 + B2`).

The API is built using **Spring Boot** and exposes endpoints for:
- Creating, reading, updating, and deleting Sheets
- Creating, reading, updating, and deleting Cells
- Evaluating formulas with dependency tracking

# Getting Started

To run the API locally (default port 8080):

## Prerequisites
- Docker installed

## Run with Docker
```bash
git clone https://github.com/justinwagoner3/json_restful_api.git
cd json_restful_api/demo
docker-compose up --build
```

## Database Connection
```sh
docker exec -it demo-mysql-demo-1 mysql -uappuser -ppassword123 demo_db
```

# Tech Stack

- Docker / Docker Compose
- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Maven
- RESTful API design
- GitHub Actions (CI/CD) – automatically build and run integration tests on pull requests

# Domain Models

## Book
```sql
CREATE TABLE books (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(255) NOT NULL,
	CONSTRAINT uc_book_name UNIQUE (`name`)
);
```
| Field   | Type   | Description          |
|---------|--------|----------------------|
| id      | int    | Unique identifier    |
| name    | string | Unique book name     |

## Sheet
```sql
CREATE TABLE sheets (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`book_id` INT NOT NULL,
	`name` VARCHAR(255) NOT NULL,
	CONSTRAINT uc_sheet_book_name UNIQUE (book_id, name),
	CONSTRAINT fk_BOOK FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);
```
| Field   | Type   | Description       |
|---------|--------|-------------------|
| id      | int    | Unique identifier |
| book_id | int    | Foreign key to the associated book |
| name    | string | Sheet name        |

## Cell
```sql
CREATE TABLE cells (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`sheet_id` INT NOT NULL,
	`row_num` INT NOT NULL,
	`col_num` VARCHAR(10) NOT NULL,
	`value` TEXT,
	`formula` TEXT,
	CONSTRAINT fk_sheet FOREIGN KEY (sheet_id) REFERENCES sheets(id) ON DELETE CASCADE
);
```
| Field     | Type   | Description                              |
|-----------|--------|------------------------------------------|
| id        | int    | Unique identifier                        |
| sheet_id  | int    | Foreign key to the sheet                 |
| row_num   | int    | Row number (e.g., 1, 2)                  |
| col_num   | string | Column name (e.g., A, B)                 |
| value     | string | Stored value or result of formula        |
| formula   | string | Optional formula (e.g., `=A1+B1`)        |

## Activity Log
```sql
CREATE TABLE activity_log (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`entity_type` VARCHAR(10) NOT NULL CHECK (entity_type IN ('BOOK', 'SHEET', 'CELL')),
	`operation` VARCHAR(10) NOT NULL CHECK (operation IN ('ADD', 'UPDATE', 'DELETE')),
	`book_id` INT NOT NULL,
	`sheet_id` INT,
	`row_num` INT,
	`col_num` VARCHAR(10),
	`value` TEXT,
	`formula` TEXT,
	`updated_by` VARCHAR(255),
	`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```
| Field       | Type                          | Description                                                                 |
|-------------|-------------------------------|-----------------------------------------------------------------------------|
| id          | int                           | Auto-incremented unique identifier for the log entry                        |
| entity_type | enum('BOOK','SHEET','CELL')   | Type of entity affected by the operation (`BOOK`, `SHEET`, or `CELL`)       |
| operation   | enum('ADD','UPDATE','DELETE') | The kind of action performed                                                |
| book_id     | int                           | Book affected (required)                                                    |
| sheet_id    | int                           | ID of the sheet involved in the operation (null if not applicable)          |
| row_num     | int                           | Row number of the affected cell (null if not applicable)                    |
| col_num     | varchar(10)                   | Column name of the affected cell (null if not applicable)                   |
| value       | text                          | Final value after the operation (e.g., raw input or calculated result)      |
| formula     | text                          | Formula associated with the cell, if any (null if not applicable)           |
| updated_by  | varchar(255)                  | Identifier of the user or system that made the change                       |
| updated_at  | timestamp                     | Timestamp of when the update occurred (auto-generated and auto-updated)     |

# Capabilities

- Create/read/update/delete Books, Sheets and Cells
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
| 404 Not Found | Object not found |
| 409 Conflict | Conflict during resource creation (e.g., duplicate) |
| 500 Internal Server Error | Server error |

# API Endpoints

## Book Endpoints

### Create
**`POST /books`** – Create a new Book  
```json
{ "name": "Book1" }
```

### Read
- **`GET /books`** – Get all Books  
- **`GET /books/{id}`** – Get a Book by ID

### Update
**`PUT /books/{id}`** – Update Book name by ID  
```json
{ "name": "Updated Book Name" }
```

### Delete
**`DELETE /books/{id}`** – Delete Book by ID

## Sheet Endpoints

### Create
**`POST /sheets`** – Create a new Sheet
Supports book object identified by id or name
```json
// Using ID
{ "name": "Sheet1", "book": { "id": 1 } }
// Using Name
{ "name": "Sheet1", "book": { "name": "Book1" } }
```

### Read
- **`GET /sheets`** – Get all Sheets  
- **`GET /sheets/{id}`** – Get a specific Sheet by ID

### Update
- **`PUT /sheets/{id}`** – Update Sheet by ID  
```json
{ "name": "Sheet1 Updated" }
```

### Delete
- **`DELETE /sheets/{id}`** – Delete Sheet by ID  
- **`DELETE /sheets`** – Delete Sheet by name and book  
Supports book object identified by id or name
```json
// Using ID
{ "name": "Sheet1", "book": { "id": 1 } }
// Using Name
{ "name": "Sheet1", "book": { "name": "Book1" } }
```

## Cell Endpoints

### Create
**`POST /cells`** – Create or update a Cell  
You can reference the sheet using either the id, or name + book:

```json
// Using Sheet ID
{
  "sheet": { "id": 1 },
  "rowNum": 1,
  "colNum": "A",
  "value": "42"
}
// Using Sheet Name + Book ID
{
  "sheet": { "name": "Sheet1", "book": { "id": 1 } },
  "rowNum": 1,
  "colNum": "A",
  "value": "42"
}
// Using Sheet Name + Book Name
{
  "sheet": { "name": "Sheet1", "book": { "name": "Book1" } },
  "rowNum": 1,
  "colNum": "A",
  "value": "42"
}
```

### Read
- **`GET /cells?sheetId=1`** – Get **all cells** from Sheet ID 1  
- **`GET /cells/{id}`** – Get a **single Cell** by ID  
- **`GET /cells/{sheetId}/{rowNum}/{colNum}`** – Get a Cell by **coordinates** in a specific sheet

### Update
- **`PUT /cells`** – Update a Cell  
(same body as `POST /cells`)

  * Processes as upsert (only true for cells)
    * 200 for updated
    * 201 for created

### Delete
- **`DELETE /cells`** – Delete a Cell by sheet + row + column  
Supports sheet by ID or name + book
```json
// Using Sheet ID
{
  "sheet": { "id": 1 },
  "rowNum": 1,
  "colNum": "A"
}
// Using Sheet Name + Book Name
{
  "sheet": { "name": "Sheet1", "book": { "name": "Book1" } },
  "rowNum": 1,
  "colNum": "A"
}
```

- **`DELETE /cells/{id}`** – Delete a Cell directly by ID  
- **`DELETE /cells/{sheetId}/{rowNum}/{colNum}`** – Delete a Cell by sheet ID and coordinates

# Complete Walkthrough

This guide demonstrates how to interact with the REST API, showcasing CRUD operations for Books, Sheets, and Cells — including proper creation order and reference behavior.

## Book API Operations

### Create Book (`Book1`)
```sh
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{ "name": "Book1" }'
```

#### Expected Response (201 Created)
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "name": "Book1"
  }
}
```

## Sheet API Operations

### Create Sheet 1 (`sheet1`) referencing Book by **name**
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet1", "book": { "name": "Book1" } }'
```

#### Expected Response (201 Created)
```json
{
  "data" : {
    "id" : 1,
    "name" : "sheet1",
    "bookId" : 1
  },
  "status" : 201
}
```

### Create Sheet 2 (`sheet2`) referencing Book by **id**
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet2", "book": { "id": 1 } }'
```

#### Expected Response (201 Created)
```json
{
  "data" : {
    "id" : 2,
    "name" : "sheet2",
    "bookId" : 1
  },
  "status" : 201
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
  "status" : 200,
  "data" : {
    "id" : 1,
    "name" : "sheet1-updated",
    "bookId" : 1
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
  "status" : 200,
  "data" : {
    "id" : 1,
    "name" : "sheet1-updated",
    "bookId" : 1
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
  "status" : 200,
  "data" : [ {
    "id" : 1,
    "name" : "sheet1-updated",
    "bookId" : 1
  } ]
}
```

## Cells API Operations

### Create Cell A1 in `sheet1-updated` using Sheet Name + Book Name
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "name": "sheet1-updated", "book": { "name": "Book1" } },
           "rowNum": 1,
           "colNum": "A",
           "value": "8"
         }'
```

#### Expected Response (201 Created)
```json
{
  "status" : 201,
  "data" : {
    "id" : 1,
    "sheetId" : 1,
    "rowNum" : 1,
    "colNum" : "A",
    "value" : "8",
    "formula" : null
  }
}
```


### Create Cell A2 using Sheet ID
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "id": 1 },
           "rowNum": 2,
           "colNum": "A",
           "value": "18"
         }'
```

#### Expected Response (201 Created)
```json
{
  "status" : 201,
  "data" : {
    "id" : 2,
    "sheetId" : 1,
    "rowNum" : 2,
    "colNum" : "A",
    "value" : "18",
    "formula" : null
  }
}
```

### Create Cell A3 with Formula `=A1+A2`
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "id": 1 },
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

### Update A2 to trigger A3 recalculation
```sh
curl -X PUT "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "id": 1 },
           "rowNum": 2,
           "colNum": "A",
           "value": "5"
         }'
```

#### Expected Response (200 OK)
```json
{
  "status" : 200,
  "data" : {
    "id" : 2,
    "sheetId" : 1,
    "rowNum" : 2,
    "colNum" : "A",
    "value" : "5",
    "formula" : null
  }
}
```

### Get A3 (should now equal 13.0)
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

### Try Creating Cell on Nonexistent Sheet
```sh
curl -X POST "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "name": "does-not-exist", "book": { "name": "Book1" } },
           "rowNum": 2,
           "colNum": "B",
           "value": "Should Fail"
         }'
```
#### Expected Response (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sheet with name \"does-not-exist\" not found.",
  "path": "/cells"
}
```

### Delete Cell A1
```sh
curl -X DELETE "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{
           "sheet": { "id": 1 },
           "rowNum": 1,
           "colNum": "A"
         }'
```

#### Expected Response (200 OK)
```json
{
  "status" : 200,
  "message" : "Cell deleted successfully"
}
```

## Database Results
```
mysql> select * from books;
+----+-------+
| id | name  |
+----+-------+
|  1 | Book1 |
+----+-------+
1 row in set (0.01 sec)

mysql> select * from sheets;
+----+---------+----------------+
| id | book_id | name           |
+----+---------+----------------+
|  1 |       1 | sheet1-updated |
+----+---------+----------------+
1 row in set (0.00 sec)

mysql> select * from cells;
+----+----------+---------+---------+-------+---------+
| id | sheet_id | row_num | col_num | value | formula |
+----+----------+---------+---------+-------+---------+
|  2 |        1 |       2 | A       | 5     | NULL    |
|  3 |        1 |       3 | A       | 5.0   | =A1+A2  |
+----+----------+---------+---------+-------+---------+

mysql> select * from activity_log;
+----+-------------+-----------+---------+----------+---------+---------+----------------+---------+------------+---------------------+
| id | entity_type | operation | book_id | sheet_id | row_num | col_num | value          | formula | updated_by | updated_at          |
+----+-------------+-----------+---------+----------+---------+---------+----------------+---------+------------+---------------------+
|  1 | BOOK        | ADD       |       1 |     NULL |    NULL | NULL    | Book1          | NULL    | system     | 2025-04-20 21:04:06 |
|  2 | SHEET       | ADD       |       1 |        1 |    NULL | NULL    | sheet1         | NULL    | system     | 2025-04-20 21:04:11 |
|  3 | SHEET       | ADD       |       1 |        2 |    NULL | NULL    | sheet2         | NULL    | system     | 2025-04-20 21:04:17 |
|  4 | SHEET       | UPDATE    |       1 |        1 |    NULL | NULL    | sheet1-updated | NULL    | system     | 2025-04-20 21:04:24 |
|  5 | SHEET       | DELETE    |       1 |        2 |    NULL | NULL    | sheet2         | NULL    | system     | 2025-04-20 21:04:45 |
|  6 | CELL        | ADD       |       1 |        1 |       1 | A       | 8              | NULL    | system     | 2025-04-20 21:04:55 |
|  7 | CELL        | ADD       |       1 |        1 |       2 | A       | 18             | NULL    | system     | 2025-04-20 21:05:04 |
|  8 | CELL        | ADD       |       1 |        1 |       3 | A       | 26.0           | =A1+A2  | system     | 2025-04-20 21:05:14 |
|  9 | CELL        | UPDATE    |       1 |        1 |       2 | A       | 5              | NULL    | system     | 2025-04-20 21:05:24 |
| 10 | CELL        | DELETE    |       1 |        1 |       1 | A       | 8              | NULL    | system     | 2025-04-20 21:06:11 |
+----+-------------+-----------+---------+----------+---------+---------+----------------+---------+------------+---------------------+
10 rows in set (0.00 sec)
```

## Summary of Features Demonstrated
* Creating a Book, then Sheets, then Cells — in correct dependency order  
* Sheet reference by both `id` and `name + book`  
* Full lifecycle of Cells: create, update, formula evaluation, delete  
* Error handling for nonexistent resources  
* Activity log recording every action with metadata

# Review and Retrospect

## Generic

1. Should have made this section at the start of the project and documented it as I went instead of drawing from memory at the end.

## Database

1. Should have switched the order of rows and cols for better query readability.
2. Generic `id` fields in db is confusing... should've done book_id, sheet_id, cell_id as PKs to make joins easier.
3. If we ended up not needing the group things by row / column frequently, might've been simpler to just have a `coordinate` field instead of row/col that would have value `A1` as example instead of splitting into two different fields.
4. Cells indexes:

Considered adding the following indexes, but was unsure how often grouping this specific would be used:

```sql
CREATE INDEX idx_cells_sheet_row ON cells(sheet_id, row_num);
CREATE INDEX idx_cells_sheet_col ON cells(sheet_id, row_col);
```

Over time we could use the access_log (activity_log) to determine if these are necessary.

## Design Consistency

1. Consistency Between CRUD Operations
    * Only `PUT /cells` supports upsert behavior (returning 201 or 200 depending on existence). For Books and Sheets, PUT operations assume the resource already exists and return 400 otherwise.

## Performance

1. Recalculating all dependents after any update is simple and correct, but might not scale well in deeply chained formulas. Dependency graph could eventually benefit from caching or smarter traversal.

## Security

1. No authentication or authorization layers exist. While out of scope for MVP, it’s important for multi-user or production use cases.
2. There's potential for formula injection or cyclic references — cycle detection isn’t yet implemented.

# Future Additions

1. History Tables
    * While the activity_log provides a chronological record of changes, implementing dedicated history tables (e.g., cell_history, sheet_history) would significantly simplify state reconstruction, auditing, and rollback functionality.
    * History tables would store full snapshots of each object on every change, making it easier to retrieve past versions or compare revisions without relying on patch-based reconstruction logic.
2. More tests
    * Explicitly test things like formula dependencies
3. Increased formula support behind simple addition
4. Allow for cross-book and cross-sheet formulas 
    * Currently they assume the cells are in the same sheet
5. Cycle detection
    * Currently, recursive or circular formula references (e.g. `A1 = A2 + 1` and `A2 = A1 + 1`) are not handled and could lead to infinite loops
6. User auditing
    * Right now, all changes are recorded as coming from "system". In a real deployment, this would be based on the authenticated user.