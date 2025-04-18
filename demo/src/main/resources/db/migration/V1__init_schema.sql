-- V1__init_schema.sql
CREATE TABLE books (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(255) NOT NULL,
	CONSTRAINT uc_book_name UNIQUE (`name`)
);

CREATE TABLE sheets (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`book_id` INT NOT NULL,
	`name` VARCHAR(255) NOT NULL,
	CONSTRAINT uc_sheet_book_name UNIQUE (book_id, name),
	CONSTRAINT fk_BOOK FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

CREATE INDEX idx_sheets_book ON sheets(book_id);

CREATE TABLE cells (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`sheet_id` INT NOT NULL,
	`row_num` INT NOT NULL,
	`col_num` VARCHAR(10) NOT NULL,
	`value` TEXT,
	`formula` TEXT,
	CONSTRAINT fk_sheet FOREIGN KEY (sheet_id) REFERENCES sheets(id) ON DELETE CASCADE
);

CREATE INDEX idx_cells_sheet ON cells(sheet_id);
CREATE INDEX idx_cells_sheet_row_col ON cells(sheet_id, row_num, col_num);

CREATE TABLE activity_log (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`entity_type` VARCHAR(10) NOT NULL CHECK (entity_type IN ('SHEET', 'CELL')),
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

CREATE INDEX idx_activity_sheet ON activity_log(sheet_id);
CREATE INDEX idx_activity_type ON activity_log(entity_type, operation);
CREATE INDEX idx_activity_updated ON activity_log(updated_by);
CREATE INDEX idx_activity_time ON activity_log(updated_at);
