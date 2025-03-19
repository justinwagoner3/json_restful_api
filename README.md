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
| created_at | timestamp    | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED                             |
| updated_at | timestamp    | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP |
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
| created_at | timestamp   | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED                             |
| updated_at | timestamp   | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED on update CURRENT_TIMESTAMP |
+------------+-------------+------+-----+-------------------+-----------------------------------------------+
```

```
mysql> desc activity_log;
+------------+--------------+------+-----+-------------------+-------------------+
| Field      | Type         | Null | Key | Default           | Extra             |
+------------+--------------+------+-----+-------------------+-------------------+
| id         | int          | NO   | PRI | NULL              | auto_increment    |
| sheet_id   | int          | NO   | MUL | NULL              |                   |
| row_num    | int          | NO   |     | NULL              |                   |
| col_num    | varchar(10)  | NO   |     | NULL              |                   |
| value      | text         | NO   |     | NULL              |                   |
| formula    | text         | YES  |     | NULL              |                   |
| updated_by | varchar(255) | NO   |     | NULL              |                   |
| updated_at | timestamp    | YES  |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
+------------+--------------+------+-----+-------------------+-------------------+
```