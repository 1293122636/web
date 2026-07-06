-- ============================================================
-- Library Full-Stack Schema — MySQL 8.0
-- ============================================================
-- 注意: 这是一个简化版 DDL，用于快速搭建项目。
-- 相比 Prisma 迁移生成的实际数据库，这里省略了：
--   - 外键约束 (FK)
--   - 部分索引
--   - ENUM 类型（改为 VARCHAR）
--   - 部分 NOT NULL 约束
-- 如需完整一致的 DDL，请用 `mysqldump --no-data library` 导出。

CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library;

-- 1. Patron Categories
DROP TABLE IF EXISTS `patron_categories`;
CREATE TABLE `patron_categories` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Item Types
DROP TABLE IF EXISTS `item_types`;
CREATE TABLE `item_types` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `loanDays` INT NOT NULL DEFAULT 0,
  `fineRate` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Circulation Rules
DROP TABLE IF EXISTS `circulation_rules`;
CREATE TABLE `circulation_rules` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `patronCategoryId` INT NOT NULL,
  `itemTypeId` INT NOT NULL,
  `maxBorrows` INT NOT NULL DEFAULT 5,
  `loanDays` INT NOT NULL DEFAULT 30,
  `renewals` INT NOT NULL DEFAULT 0,
  `renewalDays` INT NOT NULL DEFAULT 0,
  `finePerDay` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_patron_item` (`patronCategoryId`, `itemTypeId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Users
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'reader',
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `total_fines` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `patronCategoryId` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Categories
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `desc` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Books
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `isbn` VARCHAR(20) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `author` VARCHAR(200) DEFAULT NULL,
  `publisher` VARCHAR(200) DEFAULT NULL,
  `year` INT DEFAULT NULL,
  `total` INT NOT NULL DEFAULT 0,
  `available` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(20) DEFAULT 'available',
  `location` VARCHAR(200) DEFAULT NULL,
  `cover` VARCHAR(500) DEFAULT NULL,
  `desc` TEXT DEFAULT NULL,
  `clcNumber` VARCHAR(50) DEFAULT NULL,
  `physicalDesc` VARCHAR(200) DEFAULT NULL,
  `language` VARCHAR(10) DEFAULT 'chi',
  `country` VARCHAR(10) DEFAULT 'CN',
  `categoryId` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Book Items (copies)
DROP TABLE IF EXISTS `book_items`;
CREATE TABLE `book_items` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `barcode` VARCHAR(50) NOT NULL,
  `callNumber` VARCHAR(50) DEFAULT NULL,
  `location` VARCHAR(200) DEFAULT NULL,
  `condition` VARCHAR(20) DEFAULT 'normal',
  `status` VARCHAR(20) DEFAULT 'available',
  `price` DECIMAL(10,2) DEFAULT NULL,
  `acquired_at` DATETIME DEFAULT NULL,
  `notes` VARCHAR(500) DEFAULT NULL,
  `campus` VARCHAR(50) DEFAULT NULL,
  `requests` INT NOT NULL DEFAULT 0,
  `bookId` INT NOT NULL,
  `itemTypeId` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_barcode` (`barcode`),
  KEY `idx_bookId` (`bookId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Borrow Records
DROP TABLE IF EXISTS `borrow_records`;
CREATE TABLE `borrow_records` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `userId` INT NOT NULL,
  `bookId` INT NOT NULL,
  `bookItemId` INT NOT NULL,
  `borrow_date` DATETIME DEFAULT NULL,
  `due_date` DATETIME DEFAULT NULL,
  `return_date` DATETIME DEFAULT NULL,
  `renewed` TINYINT(1) DEFAULT 0,
  `status` VARCHAR(20) DEFAULT 'active',
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  `updated_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`),
  KEY `idx_userId` (`userId`),
  KEY `idx_bookId` (`bookId`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Fines
DROP TABLE IF EXISTS `fines`;
CREATE TABLE `fines` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `borrowRecordId` INT DEFAULT NULL,
  `userId` INT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `type` VARCHAR(20) DEFAULT 'overdue',
  `paid` TINYINT(1) DEFAULT 0,
  `paid_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Holds
DROP TABLE IF EXISTS `holds`;
CREATE TABLE `holds` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `userId` INT NOT NULL,
  `bookId` INT NOT NULL,
  `bookItemId` INT DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `request_date` DATETIME DEFAULT NULL,
  `expiry_date` DATETIME DEFAULT NULL,
  `fulfilled_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Audit Logs
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `userId` INT DEFAULT NULL,
  `action` VARCHAR(100) DEFAULT NULL,
  `target` VARCHAR(100) DEFAULT NULL,
  `detail` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT NOW(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
