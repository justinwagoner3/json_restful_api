-- V1__init_schema.sql

CREATE TABLE sheets (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`name` VARCHAR(255) NOT NULL
);

CREATE TABLE cells (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`sheet_id` INT NOT NULL,
	`row_num` INT NOT NULL,
	`col_num` VARCHAR(10) NOT NULL,
	`value` TEXT,
	`formula` TEXT,
	CONSTRAINT fk_sheet FOREIGN KEY (sheet_id) REFERENCES sheets(id) ON DELETE CASCADE
);

CREATE TABLE activity_log (
	`id` INT AUTO_INCREMENT PRIMARY KEY,
	`entity_type` VARCHAR(10) NOT NULL CHECK (entity_type IN ('SHEET', 'CELL')),
	`operation` VARCHAR(10) NOT NULL CHECK (operation IN ('ADD', 'UPDATE', 'DELETE')),
	`sheet_id` INT NOT NULL,
	`row_num` INT,
	`col_num` VARCHAR(10),
	`value` TEXT,
	`formula` TEXT,
	`updated_by` VARCHAR(255),
	`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	FOREIGN KEY (sheet_id) REFERENCES sheets(id) ON DELETE CASCADE
);
