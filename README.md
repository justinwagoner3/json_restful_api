# Complete Walkthrough
# API Walkthrough: Sheets & Cells Management

This guide demonstrates how to interact with the REST API, showcasing CRUD operations for sheets and cells.

## **1️⃣ Sheets API Operations**

### **Create Sheet 1 (`sheet1`)**
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet1" }'
```
#### **Expected Response (201 Created)**
```json
{
  "status": 201,
  "data": {
    "id": 1,
    "name": "sheet1"
  }
}
```

### **Create Sheet 2 (`sheet2`)**
```sh
curl -X POST "http://localhost:8080/sheets" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet2" }'
```
#### **Expected Response (201 Created)**
```json
{
  "status": 201,
  "data": {
    "id": 2,
    "name": "sheet2"
  }
}
```

### **Update `sheet1` to `sheet1-updated`**
```sh
curl -X PUT "http://localhost:8080/sheets/1" \
     -H "Content-Type: application/json" \
     -d '{ "name": "sheet1-updated" }'
```
#### **Expected Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "name": "sheet1-updated"
  }
}
```

### **Get Sheet by ID (`sheet1-updated`)**
```sh
curl -X GET "http://localhost:8080/sheets/1"
```
#### **Expected Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "name": "sheet1-updated"
  }
}
```

### **Delete `sheet2`**
```sh
curl -X DELETE "http://localhost:8080/sheets/2"
```
#### **Expected Response (200 OK)**
```json
{
  "status": 200,
  "message": "Sheet deleted successfully"
}
```

### **Get All Sheets (Only `sheet1-updated` Should Exist)**
```sh
curl -X GET "http://localhost:8080/sheets"
```
#### **Expected Response (200 OK)**
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

---

## **2️⃣ Cells API Operations**

### **Create Cells A1 and A2 in `sheet1-updated`**
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

### **Create a Formula Cell `=A1+A2` in Row 3**
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
#### **Expected Response (201 Created)**
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

### **Update A2 to a New Value (Triggers A3 to Update)**
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

### **Verify that A3 Now Shows `13.0` (8 + 5)**
```sh
curl -X GET "http://localhost:8080/cells/1/3/A"
```
#### **Expected Response (200 OK)**
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

### **Try Creating a Cell in a Non-Existent Sheet**
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
#### **Expected Response (404 Not Found)**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sheet with name \"nonexistent-sheet\" not found.",
  "path": "/cells"
}
```

### **Delete Cell from `sheet1-updated`**
```sh
curl -X DELETE "http://localhost:8080/cells" \
     -H "Content-Type: application/json" \
     -d '{ 
           "sheet": { "name": "sheet1-updated" },
           "rowNum": 1,
           "colNum": "A"
         }'
```
#### **Expected Response (200 OK)**
```json
{
  "status": 200,
  "message": "Cell deleted successfully"
}
```

---

## **🎯 Summary of Features Demonstrated:**
✅ Creating, updating, retrieving, and deleting **sheets**.  
✅ Creating, updating, retrieving, and deleting **cells**.  
✅ Error handling for **nonexistent sheets**.  
✅ **Formula dependency tracking**: changes in A1/A2 update A3.  
✅ Final verification to confirm changes.  

This guide provides a **quick and structured** way to showcase API functionality in an interview. 🚀

# Packages

* Spring Web (for REST API)
* Spring Data JPA (for database interaction)
* MySQL Driver (since the job description references MySQL)

# Table Definitions

```
mysql> desc sheets;
+------------+--------------+------+-----+-------------------+-----------------------------------------------+
| Field      | Type         | Null | Key | Default           | Extra                                         |
+------------+--------------+------+-----+-------------------+-----------------------------------------------+
| id         | int          | NO   | PRI | NULL              | auto_increment                                |
| name       | varchar(255) | NO   |     | NULL              |                                               |
+------------+--------------+------+-----+-------------------+-----------------------------------------------+
```

```
mysql> desc cells;
+------------+-------------+------+-----+-------------------+-----------------------------------------------+
| Field      | Type        | Null | Key | Default           | Extra                                         |
+------------+-------------+------+-----+-------------------+-----------------------------------------------+
| id         | int         | NO   | PRI | NULL              | auto_increment                                |
| sheet_id   | int         | NO   | MUL | NULL              |                                               |
| row_num    | int         | NO   |     | NULL              |                                               |
| col_num    | varchar(10) | NO   |     | NULL              |                                               |
| value      | text        | YES  |     | NULL              |                                               |
| formula    | text        | YES  |     | NULL              |                                               |
+------------+-------------+------+-----+-------------------+-----------------------------------------------+
```

```
mysql> desc activity_log;
+-------------+-------------------------------+------+-----+-------------------+-----------------------------------------------+
| Field       | Type                          | Null | Key | Default           | Extra                                         |
+-------------+-------------------------------+------+-----+-------------------+-----------------------------------------------+
| id          | int                           | NO   | PRI | NULL              | auto_increment                                |
| entity_type | enum('SHEET','CELL')          | NO   |     | NULL              |                                               |
| operation   | enum('ADD','UPDATE','DELETE') | NO   |     | NULL              |                                               |
| sheet_id    | int                           | NO   | MUL | NULL              |                                               |
| row_num     | int                           | YES  |     | NULL              |                                               |
| col_num     | varchar(10)                   | YES  |     | NULL              |                                               |
| value       | text                          | NO   |     | NULL              |                                               |
| formula     | text                          | YES  |     | NULL              |                                               |
| updated_by  | varchar(255)                  | NO   |     | NULL              |                                               |
| updated_at  | timestamp                     | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP |
+-------------+-------------------------------+------+-----+-------------------+-----------------------------------------------+
```
