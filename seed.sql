-- ============================================================
-- Library Full-Stack Seed — matching original Prisma seed data
-- ============================================================
-- Run: mysql -h127.0.0.1 -uroot -p library < this_file

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE audit_logs;
TRUNCATE TABLE fines;
TRUNCATE TABLE holds;
TRUNCATE TABLE borrow_records;
TRUNCATE TABLE book_items;
TRUNCATE TABLE books;
TRUNCATE TABLE circulation_rules;
TRUNCATE TABLE item_types;
TRUNCATE TABLE patron_categories;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ===== 1. Patron Categories =====
INSERT INTO patron_categories (id, name, created_at) VALUES
(1, '本科生', NOW()),
(2, '研究生', NOW()),
(3, '教师', NOW());

-- ===== 2. Item Types =====
INSERT INTO item_types (id, name, loanDays, fineRate, created_at) VALUES
(1, '普通图书', 30, 0.10, NOW()),
(2, '新书速递', 7,  0.20, NOW()),
(3, '工具书',   0,  0.00, NOW());

-- ===== 3. Circulation Rules (3x3) =====
INSERT INTO circulation_rules (patronCategoryId, itemTypeId, maxBorrows, loanDays, renewals, renewalDays, finePerDay, created_at) VALUES
-- 本科生 × all item types
(1, 1, 5,  30, 1, 15, 0.10, NOW()),
(1, 2, 5,  7,  1, 15, 0.10, NOW()),
(1, 3, 0,  0,  1, 15, 0.10, NOW()),
-- 研究生 × all
(2, 1, 10, 30, 2, 30, 0.20, NOW()),
(2, 2, 10, 7,  2, 30, 0.20, NOW()),
(2, 3, 0,  0,  2, 30, 0.20, NOW()),
-- 教师 × all
(3, 1, 20, 30, 3, 60, 0.50, NOW()),
(3, 2, 20, 7,  3, 60, 0.50, NOW()),
(3, 3, 0,  0,  3, 60, 0.50, NOW());

-- ===== 4. Users (8 readers + 1 admin) =====
-- bcrypt hashes: reader123 / admin123
INSERT INTO users (id, username, password, name, role, phone, email, patronCategoryId, total_fines, created_at, updated_at) VALUES
(1,  'admin',       '$2a$10$YU3G2ym8V91HTnjEx4rZgeA8bOawPIH0.O3mIioRsuIJ/P8F4olD6', '系统管理员', 'admin', NULL, NULL, NULL, 0.00, NOW(), NOW()),
(2,  '2023110101',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '张三',   'reader', '13800000001', '2023110101@sdust.edu.cn', 1, 0.00, NOW(), NOW()),
(3,  '2022110201',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '李四',   'reader', '13800000002', '2022110201@sdust.edu.cn', 2, 60.00, NOW(), NOW()),
(4,  'T2023001',    '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '王五',   'reader', '13800000003', 'wangwu@sdust.edu.cn', 3, 0.00, NOW(), NOW()),
(5,  '2024110301',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '赵六',   'reader', '13800000004', '2024110301@sdust.edu.cn', 1, 0.00, NOW(), NOW()),
(6,  '2023110202',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '孙七',   'reader', '13800000005', '2023110202@sdust.edu.cn', 2, 0.00, NOW(), NOW()),
(7,  'T2024002',    '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '周八',   'reader', '13800000006', 'zhouba@sdust.edu.cn', 3, 0.00, NOW(), NOW()),
(8,  '2024110401',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '吴九',   'reader', '13800000007', '2024110401@sdust.edu.cn', 1, 0.00, NOW(), NOW()),
(9,  '2022110302',  '$2a$10$bQhPxd4J2c629Tw9S/1zGuwEG3e.7Cj/xNtntoK3waGU3KsxevI5G', '郑十',   'reader', '13800000008', '2022110302@sdust.edu.cn', 2, 0.00, NOW(), NOW());

-- ===== 5. Categories (5) =====
INSERT INTO categories (id, name, `desc`, created_at, updated_at) VALUES
(1, '计算机科学', '计算机编程、算法、人工智能等', NOW(), NOW()),
(2, '文学小说',   '中外文学名著、小说、散文', NOW(), NOW()),
(3, '自然科学',   '物理、数学、天文、生物', NOW(), NOW()),
(4, '历史哲学',   '中外历史、哲学思想', NOW(), NOW()),
(5, '经济管理',   '经济学、管理学、商业', NOW(), NOW());

-- ===== 6. Books (20种) =====
INSERT INTO books (id, isbn, title, author, publisher, year, total, available, status, clcNumber, physicalDesc, language, country, categoryId, `desc`, cover, created_at, updated_at) VALUES
-- 计算机科学 (cat=1)
(1,  '978-7-111-58444-5', '算法导论',             'Thomas H. Cormen',  '机械工业出版社',   2013, 5, 0, 'available', 'TP301.6',  '780页', 'chi', 'CN', 1, '算法导论 — Thomas H. Cormen 著', 'https://img.alicdn.com/bao/uploaded/i1/2744812128/O1CN01qKXgUG1RafRgDyVOu_!!0-item_pic.jpg', NOW(), NOW()),
(2,  '978-7-111-52944-6', '深入理解计算机系统',     'Randal E. Bryant',  '机械工业出版社',   2016, 3, 3, 'available', 'TP303',    '737页', 'chi', 'CN', 1, '深入理解计算机系统 — Randal E. Bryant 著', 'https://www0.kfzimg.com/sw/kfz-cos/kfzimg/acbebccc/d7af732c46f585a3_s.jpg', NOW(), NOW()),
(3,  '978-7-302-57486-0', '机器学习',              '周志华',            '清华大学出版社',   2016, 3, 2, 'available', 'TP181',    '425页', 'chi', 'CN', 1, '机器学习 — 周志华 著', 'https://img11.360buyimg.com/n1/jfs/t1/54802/36/8844/217804/5d6388dbE4a088e81/05d1646fcc28420b.jpg', NOW(), NOW()),
(4,  '978-7-121-41559-1', 'Python深度学习',        'François Chollet',  '电子工业出版社',   2022, 3, 3, 'available', 'TP311.56', '481页', 'chi', 'CN', 1, 'Python深度学习 — François Chollet 著', 'https://www0.kfzimg.com/sw/kfz-cos/kfzimg/cddafecc/bffd05aa3fa8925e_n.jpg', NOW(), NOW()),
(5,  '978-7-115-60960-1', '计算机网络：自顶向下',   'James Kurose',      '人民邮电出版社',   2022, 3, 2, 'available', 'TP393',    '684页', 'chi', 'CN', 1, '计算机网络：自顶向下 — James Kurose 著', 'https://img.alicdn.com/i1/3446196188/O1CN01jZVOSy1va9YsZXryG_!!3446196188.jpg', NOW(), NOW()),
(6,  '0-201-61586-X',     'Computer Networks',     'Andrew Tanenbaum',  'Prentice Hall',    2017, 3, 3, 'available', 'TP393',    '960pp', 'eng', 'US', 1, 'Computer Networks — Andrew Tanenbaum 著', 'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1347462821i/166190.jpg', NOW(), NOW()),
-- 文学小说 (cat=2)
(7,  '978-7-02-000220-7', '红楼梦',                '曹雪芹',            '人民文学出版社',   1996, 4, 3, 'available', 'I242.4',   '1606页','chi', 'CN', 2, '红楼梦 — 曹雪芹 著', 'https://booklibimg.kfzimg.com/data/book_lib_img_v2/user/1/ebcd/ebcd90378f6f38976296a22929a1e2e9_0_1_300_300.jpg', NOW(), NOW()),
(8,  '978-7-5447-4254-2', '百年孤独',              '加西亚·马尔克斯',    '南海出版公司',     2011, 3, 3, 'available', 'I775.45',  '360页', 'chi', 'CN', 2, '百年孤独 — 加西亚·马尔克斯 著', 'https://img.alicdn.com/bao/uploaded/i1/1086465456/TB1EN7iOXXXXXc0apXXXXXXXXXX_!!0-item_pic.jpg', NOW(), NOW()),
(9,  '978-7-5063-5020-8', '平凡的世界',            '路遥',              '作家出版社',       2012, 3, 3, 'available', 'I247.5',   '1296页','chi', 'CN', 2, '平凡的世界 — 路遥 著', 'https://img.alicdn.com/bao/uploaded/i3/720096711/O1CN01f4KpcD1zRggmBcsgz_!!2-item_pic.png', NOW(), NOW()),
(10, '978-7-5404-7062-5', '活着',                  '余华',              '湖南文艺出版社',   2017, 3, 3, 'available', 'I247.57',  '191页', 'chi', 'CN', 2, '活着 — 余华 著', 'https://img14.360buyimg.com/pop/jfs/t1/202390/35/17788/414539/61b1d057E5f58c1db/ed08fade1834dffd.png', NOW(), NOW()),
-- 自然科学 (cat=3)
(11, '978-7-301-27701-2', '时间简史',              '史蒂芬·霍金',       '北京大学出版社',   1998, 3, 2, 'available', 'P159-49',  '212页', 'chi', 'CN', 3, '时间简史 — 史蒂芬·霍金 著', 'https://img10.360buyimg.com/n1/s720x720_jfs/t1/373056/11/3089/98455/69328cd2F4fec62f5/55790ff891400619.jpg', NOW(), NOW()),
(12, '978-7-5536-4786-9', '上帝掷骰子吗',          '曹天元',            '浙江教育出版社',   2018, 3, 3, 'available', 'O413-49',  '420页', 'chi', 'CN', 3, '上帝掷骰子吗 — 曹天元 著', 'https://img12.360buyimg.com/n1/jfs/t331/144/2049514225/87843/aae4d47c/544e0afaN656f5eda.jpg', NOW(), NOW()),
(13, '978-7-302-57834-0', '从一到无穷大',          '乔治·伽莫夫',       '清华大学出版社',   2020, 3, 3, 'available', 'N49',      '328页', 'chi', 'CN', 3, '从一到无穷大 — 乔治·伽莫夫 著', 'https://img13.360buyimg.com/n1/jfs/t1/85354/1/17985/213624/5e8ef59bE379d1b17/01eb5584381ac943.jpg', NOW(), NOW()),
-- 历史哲学 (cat=4)
(14, '978-7-108-04436-5', '万历十五年',            '黄仁宇',            '生活·读书·新知三联书店', 1997, 3, 3, 'available', 'K248.307', '386页', 'chi', 'CN', 4, '万历十五年 — 黄仁宇 著', 'https://img11.360buyimg.com/n1/jfs/t1/153654/33/7399/210089/5fbf8b7eE5ba7f627/e52923825b6c1d7a.png', NOW(), NOW()),
(15, '978-7-220-10795-7', '人类简史',              '尤瓦尔·赫拉利',     '四川人民出版社',   2021, 3, 2, 'available', 'K02-49',   '440页', 'chi', 'CN', 4, '人类简史 — 尤瓦尔·赫拉利 著', 'https://img.alicdn.com/i2/101450072/O1CN0112Kj9F1CP1GuGqRvS-101450072.png', NOW(), NOW()),
(16, '978-7-108-06904-7', '中国哲学简史',          '冯友兰',            '生活·读书·新知三联书店', 2019, 3, 3, 'available', 'B2',       '416页', 'chi', 'CN', 4, '中国哲学简史 — 冯友兰 著', 'https://www0.kfzimg.com/sw/kfz-cos/kfzimg/11524160/590ae5a3a054dafb_n.jpg', NOW(), NOW()),
-- 经济管理 (cat=5)
(17, '978-7-5086-5191-5', '从0到1',                '彼得·蒂尔',         '中信出版社',       2015, 3, 3, 'available', 'F272',     '260页', 'chi', 'CN', 5, '从0到1 — 彼得·蒂尔 著', 'https://img.alicdn.com/i1/859515618/O1CN016UtoEJ1rN5iXO1vAb_!!859515618.jpg', NOW(), NOW()),
(18, '978-7-5086-6081-8', '思考，快与慢',          '丹尼尔·卡尼曼',     '中信出版社',       2020, 3, 2, 'available', 'F069.9',   '484页', 'chi', 'CN', 5, '思考，快与慢 — 丹尼尔·卡尼曼 著', 'https://img.alicdn.com/bao/uploaded/O1CN01Dopa1C1S86SvbY3qZ_!!6000000002201-0-yinhe.jpg', NOW(), NOW()),
(19, '978-7-5049-9117-9', '国富论',                '亚当·斯密',         '中国人民大学出版社',2021, 3, 3, 'available', 'F091.33',  '540页', 'chi', 'CN', 5, '国富论 — 亚当·斯密 著', 'https://booklibimg.kfzimg.com/data/book_lib_img_v2/isbn/1/2650/26501c0b622acf0590a29b60b918ac7c_0_1_300_300.jpg', NOW(), NOW()),
(20, '978-4-00-331041-5', 'PHP是世界上最好的语言',  '佚名',              '技术出版社',       2024, 3, 3, 'available', 'TP312',    '200页', 'jpn', 'JP', 1, 'PHP是世界上最好的语言 — 佚名 著', 'https://www.21cto.com/uploads/article/20170509/2bf93f173a3490a4a9a004016362691d.png', NOW(), NOW());

-- ===== 7. Book Items (copies) =====
-- 算法导论: 5 copies (id=1)
INSERT INTO book_items (barcode, callNumber, location, campus, `condition`, status, price, bookId, itemTypeId, acquired_at, updated_at) VALUES
('LIB-000001-1', 'TP301.6/1001', '青岛馆A区3楼', '青岛', 'normal', 'borrowed', 59.00, 1, 1, '2025-01-01', NOW()),
('LIB-000001-2', 'TP301.6/1002', '青岛馆B区2楼', '青岛', 'normal', 'borrowed', 69.00, 1, 1, '2025-01-01', NOW()),
('LIB-000001-3', 'TP301.6/1003', '青岛8楼阅览区', '青岛', 'normal', 'borrowed', 79.00, 1, 1, '2025-01-01', NOW()),
('LIB-000001-4', 'TP301.6/1004', '青岛馆A区3楼', '青岛', 'normal', 'available', 89.00, 1, 1, '2025-01-01', NOW()),
('LIB-000001-5', 'TP301.6/1005', '青岛馆B区2楼', '青岛', 'normal', 'available', 99.00, 1, 1, '2025-01-01', NOW()),
-- 深入理解计算机系统: 3 copies (id=2)
('LIB-000002-1', 'TP303/1002', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 2, 1, '2025-01-01', NOW()),
('LIB-000002-2', 'TP303/1003', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 2, 1, '2025-01-01', NOW()),
('LIB-000002-3', 'TP303/1004', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 2, 1, '2025-01-01', NOW()),
-- 机器学习: 3 copies (id=3) — 1 set to on_hold for demo
('LIB-000003-1', 'TP181/1003', '青岛馆A区3楼', '青岛', 'normal', 'on_hold', 59.00, 3, 1, '2025-01-01', NOW()),
('LIB-000003-2', 'TP181/1004', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 3, 1, '2025-01-01', NOW()),
('LIB-000003-3', 'TP181/1005', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 3, 1, '2025-01-01', NOW()),
-- Python深度学习: 3 copies (id=4)
('LIB-000004-1', 'TP311.56/1004', '泰安馆自科借阅区', '泰安', 'normal', 'available', 59.00, 4, 1, '2025-01-01', NOW()),
('LIB-000004-2', 'TP311.56/1005', '泰安馆社科借阅区', '泰安', 'normal', 'available', 69.00, 4, 1, '2025-01-01', NOW()),
('LIB-000004-3', 'TP311.56/1006', '泰安馆自科借阅区', '泰安', 'normal', 'available', 79.00, 4, 1, '2025-01-01', NOW()),
-- 计算机网络: 3 copies (id=5)
('LIB-000005-1', 'TP393/1005', '济南馆自科借阅区', '济南', 'normal', 'borrowed', 59.00, 5, 1, '2025-01-01', NOW()),
('LIB-000005-2', 'TP393/1006', '济南馆社科借阅区', '济南', 'normal', 'available', 69.00, 5, 1, '2025-01-01', NOW()),
('LIB-000005-3', 'TP393/1007', '济南馆自科借阅区', '济南', 'normal', 'available', 79.00, 5, 1, '2025-01-01', NOW()),
-- Computer Networks: 3 copies (id=6)
('LIB-000006-1', 'TP393/1006', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 6, 1, '2025-01-01', NOW()),
('LIB-000006-2', 'TP393/1007', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 6, 1, '2025-01-01', NOW()),
('LIB-000006-3', 'TP393/1008', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 6, 1, '2025-01-01', NOW()),
-- 红楼梦: 4 copies (id=7)
('LIB-000007-1', 'I242.4/1007', '青岛馆A区3楼', '青岛', 'normal', 'borrowed', 49.00, 7, 1, '2025-01-01', NOW()),
('LIB-000007-2', 'I242.4/1008', '青岛馆B区2楼', '青岛', 'normal', 'available', 59.00, 7, 1, '2025-01-01', NOW()),
('LIB-000007-3', 'I242.4/1009', '青岛8楼阅览区', '青岛', 'normal', 'available', 69.00, 7, 1, '2025-01-01', NOW()),
('LIB-000007-4', 'I242.4/1010', '青岛馆A区3楼', '青岛', 'normal', 'available', 79.00, 7, 1, '2025-01-01', NOW()),
-- 百年孤独: 3 copies (id=8)
('LIB-000008-1', 'I775.45/1008', '泰安馆自科借阅区', '泰安', 'normal', 'available', 59.00, 8, 1, '2025-01-01', NOW()),
('LIB-000008-2', 'I775.45/1009', '泰安馆社科借阅区', '泰安', 'normal', 'available', 69.00, 8, 1, '2025-01-01', NOW()),
('LIB-000008-3', 'I775.45/1010', '泰安馆自科借阅区', '泰安', 'normal', 'available', 79.00, 8, 1, '2025-01-01', NOW()),
-- 平凡的世界: 3 copies (id=9)
('LIB-000009-1', 'I247.5/1009', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 9, 1, '2025-01-01', NOW()),
('LIB-000009-2', 'I247.5/1010', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 9, 1, '2025-01-01', NOW()),
('LIB-000009-3', 'I247.5/1011', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 9, 1, '2025-01-01', NOW()),
-- 活着: 3 copies (id=10)
('LIB-000010-1', 'I247.57/1010', '泰安馆自科借阅区', '泰安', 'normal', 'available', 59.00, 10, 1, '2025-01-01', NOW()),
('LIB-000010-2', 'I247.57/1011', '泰安馆社科借阅区', '泰安', 'normal', 'available', 69.00, 10, 1, '2025-01-01', NOW()),
('LIB-000010-3', 'I247.57/1012', '泰安馆自科借阅区', '泰安', 'normal', 'available', 79.00, 10, 1, '2025-01-01', NOW()),
-- 时间简史: 3 copies (id=11)
('LIB-000011-1', 'P159-49/1011', '青岛馆A区3楼', '青岛', 'normal', 'borrowed', 59.00, 11, 1, '2025-01-01', NOW()),
('LIB-000011-2', 'P159-49/1012', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 11, 1, '2025-01-01', NOW()),
('LIB-000011-3', 'P159-49/1013', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 11, 1, '2025-01-01', NOW()),
-- 上帝掷骰子吗: 3 copies (id=12)
('LIB-000012-1', 'O413-49/1012', '济南馆自科借阅区', '济南', 'normal', 'available', 59.00, 12, 1, '2025-01-01', NOW()),
('LIB-000012-2', 'O413-49/1013', '济南馆社科借阅区', '济南', 'normal', 'available', 69.00, 12, 1, '2025-01-01', NOW()),
('LIB-000012-3', 'O413-49/1014', '济南馆自科借阅区', '济南', 'normal', 'available', 79.00, 12, 1, '2025-01-01', NOW()),
-- 从一到无穷大: 3 copies (id=13)
('LIB-000013-1', 'N49/1013', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 13, 1, '2025-01-01', NOW()),
('LIB-000013-2', 'N49/1014', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 13, 1, '2025-01-01', NOW()),
('LIB-000013-3', 'N49/1015', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 13, 1, '2025-01-01', NOW()),
-- 万历十五年: 3 copies (id=14)
('LIB-000014-1', 'K248.307/1014', '济南馆自科借阅区', '济南', 'normal', 'available', 59.00, 14, 1, '2025-01-01', NOW()),
('LIB-000014-2', 'K248.307/1015', '济南馆社科借阅区', '济南', 'normal', 'available', 69.00, 14, 1, '2025-01-01', NOW()),
('LIB-000014-3', 'K248.307/1016', '济南馆自科借阅区', '济南', 'normal', 'available', 79.00, 14, 1, '2025-01-01', NOW()),
-- 人类简史: 3 copies (id=15)
('LIB-000015-1', 'K02-49/1015', '青岛馆A区3楼', '青岛', 'normal', 'borrowed', 59.00, 15, 1, '2025-01-01', NOW()),
('LIB-000015-2', 'K02-49/1016', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 15, 1, '2025-01-01', NOW()),
('LIB-000015-3', 'K02-49/1017', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 15, 1, '2025-01-01', NOW()),
-- 中国哲学简史: 3 copies (id=16)
('LIB-000016-1', 'B2/1016', '泰安馆自科借阅区', '泰安', 'normal', 'available', 59.00, 16, 1, '2025-01-01', NOW()),
('LIB-000016-2', 'B2/1017', '泰安馆社科借阅区', '泰安', 'normal', 'available', 69.00, 16, 1, '2025-01-01', NOW()),
('LIB-000016-3', 'B2/1018', '泰安馆自科借阅区', '泰安', 'normal', 'available', 79.00, 16, 1, '2025-01-01', NOW()),
-- 从0到1: 3 copies (id=17)
('LIB-000017-1', 'F272/1017', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 17, 1, '2025-01-01', NOW()),
('LIB-000017-2', 'F272/1018', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 17, 1, '2025-01-01', NOW()),
('LIB-000017-3', 'F272/1019', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 17, 1, '2025-01-01', NOW()),
-- 思考快与慢: 3 copies (id=18)
('LIB-000018-1', 'F069.9/1018', '青岛馆A区3楼', '青岛', 'normal', 'borrowed', 59.00, 18, 1, '2025-01-01', NOW()),
('LIB-000018-2', 'F069.9/1019', '泰安馆自科借阅区', '泰安', 'normal', 'available', 69.00, 18, 1, '2025-01-01', NOW()),
('LIB-000018-3', 'F069.9/1020', '泰安馆社科借阅区', '泰安', 'normal', 'available', 79.00, 18, 1, '2025-01-01', NOW()),
-- 国富论: 3 copies (id=19)
('LIB-000019-1', 'F091.33/1019', '济南馆自科借阅区', '济南', 'normal', 'available', 59.00, 19, 1, '2025-01-01', NOW()),
('LIB-000019-2', 'F091.33/1020', '济南馆社科借阅区', '济南', 'normal', 'available', 69.00, 19, 1, '2025-01-01', NOW()),
('LIB-000019-3', 'F091.33/1021', '济南馆自科借阅区', '济南', 'normal', 'available', 79.00, 19, 1, '2025-01-01', NOW()),
-- PHP书: 3 copies (id=20)
('LIB-000020-1', 'TP312/1020', '青岛馆A区3楼', '青岛', 'normal', 'available', 59.00, 20, 1, '2025-01-01', NOW()),
('LIB-000020-2', 'TP312/1021', '青岛馆B区2楼', '青岛', 'normal', 'available', 69.00, 20, 1, '2025-01-01', NOW()),
('LIB-000020-3', 'TP312/1022', '青岛8楼阅览区', '青岛', 'normal', 'available', 79.00, 20, 1, '2025-01-01', NOW());

-- ===== 8. Borrow Records =====
-- 张三 (id=2)·本科生: 2 active + 2 returned/overdue + 1 history
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(2, 1, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL -10 DAY), NULL, 'active', false, NOW(), NOW()),
(2, 3, 10, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL -15 DAY), NULL, 'active', false, NOW(), NOW()),
(2, 9, 28, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), 'returned', true, NOW(), NOW()),
(2, 10, 31, DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 'overdue', false, NOW(), NOW()),
(2, 4, 12, DATE_SUB(NOW(), INTERVAL 120 DAY), DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 88 DAY), 'returned', false, NOW(), NOW());
-- 李四 (id=3)·研究生: 3 active + 1 overdue
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(3, 5, 15, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL -50 DAY), NULL, 'active', false, NOW(), NOW()),
(3, 15, 43, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL -55 DAY), NULL, 'active', true, NOW(), NOW()),
(3, 7, 22, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL -30 DAY), NULL, 'active', false, NOW(), NOW()),
(3, 6, 18, DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), NULL, 'active', false, NOW(), NOW());
-- 王五 (id=4)·教师
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(4, 18, 53, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL -173 DAY), NULL, 'active', false, NOW(), NOW()),
(4, 13, 39, DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 140 DAY), DATE_SUB(NOW(), INTERVAL 138 DAY), 'returned', true, NOW(), NOW()),
(4, 12, 36, DATE_SUB(NOW(), INTERVAL 150 DAY), DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 88 DAY), 'returned', true, NOW(), NOW());
-- 赵六 (id=5)·本科生
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(5, 11, 33, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL -5 DAY), NULL, 'active', false, NOW(), NOW()),
(5, 14, 42, DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), 'returned', false, NOW(), NOW());
-- 孙七 (id=6)·研究生 (extra history for stats)
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(6, 1, 2, DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 270 DAY), DATE_SUB(NOW(), INTERVAL 268 DAY), 'returned', false, NOW(), NOW()),
(6, 2, 5, DATE_SUB(NOW(), INTERVAL 250 DAY), DATE_SUB(NOW(), INTERVAL 220 DAY), DATE_SUB(NOW(), INTERVAL 218 DAY), 'returned', false, NOW(), NOW()),
(6, 3, 11, DATE_SUB(NOW(), INTERVAL 180 DAY), DATE_SUB(NOW(), INTERVAL 150 DAY), DATE_SUB(NOW(), INTERVAL 148 DAY), 'returned', false, NOW(), NOW()),
(6, 18, 54, DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 58 DAY), 'returned', false, NOW(), NOW());
-- 周八 (id=7)·教师
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(7, 17, 49, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL -177 DAY), NULL, 'active', false, NOW(), NOW());
-- 吴九 (id=8)·本科生
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(8, 7, 23, DATE_SUB(NOW(), INTERVAL 130 DAY), DATE_SUB(NOW(), INTERVAL 100 DAY), DATE_SUB(NOW(), INTERVAL 98 DAY), 'returned', true, NOW(), NOW()),
(8, 8, 25, DATE_SUB(NOW(), INTERVAL 80 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 48 DAY), 'returned', false, NOW(), NOW());
-- 郑十 (id=9)·研究生
INSERT INTO borrow_records (userId, bookId, bookItemId, borrow_date, due_date, return_date, status, renewed, created_at, updated_at) VALUES
(9, 1, 3, DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 140 DAY), DATE_SUB(NOW(), INTERVAL 138 DAY), 'returned', true, NOW(), NOW()),
(9, 5, 16, DATE_SUB(NOW(), INTERVAL 70 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, 'active', true, NOW(), NOW());

-- ===== 9. Fines =====
-- 张三逾期还书: borrow_record user_id=2, book_id=10 (ID=4)
-- 李四逾期未还: borrow_record user_id=3, book_id=6   (ID=9)
-- We'll use direct subqueries
INSERT INTO fines (borrowRecordId, userId, amount, type, paid, paid_at, created_at)
SELECT br.id, 2, 5.00, 'overdue', true, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()
FROM borrow_records br WHERE br.userId = 2 AND br.bookId = 10 LIMIT 1;

INSERT INTO fines (borrowRecordId, userId, amount, type, paid, paid_at, created_at)
SELECT br.id, 3, 60.00, 'overdue', false, NULL, NOW()
FROM borrow_records br WHERE br.userId = 3 AND br.bookId = 6 LIMIT 1;

-- ===== 10. Update user total_fines =====
UPDATE users SET total_fines = 60.00 WHERE id = 3;

-- ===== 11. Holds =====
-- 算法导论 (book=1): set available=0 to force holds
UPDATE books SET available = 0 WHERE id = 1;

-- 赵六(5) & 孙七(6) 预约算法导论
INSERT INTO holds (userId, bookId, status, request_date, created_at) VALUES
(5, 1, 'pending', NOW(), NOW()),
(6, 1, 'pending', NOW(), NOW());

-- 机器学习 (book=3): ready hold for 张三(2)
UPDATE books SET available = available - 1 WHERE id = 3;
INSERT INTO holds (userId, bookId, bookItemId, status, request_date, expiry_date, created_at)
SELECT 2, 3, bi.id, 'ready', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), NOW()
FROM book_items bi WHERE bi.barcode = 'LIB-000003-1';

-- ===== 12. Update book available counts =====
UPDATE books b SET available = (
  SELECT COUNT(*) FROM book_items bi WHERE bi.bookId = b.id AND bi.status = 'available'
);
UPDATE books SET available = 0 WHERE id = 1;

-- ===== 13. Verify =====
SELECT CONCAT('Seed complete: ',
  (SELECT COUNT(*) FROM users), ' users, ',
  (SELECT COUNT(*) FROM patron_categories), ' patron categories, ',
  (SELECT COUNT(*) FROM item_types), ' item types, ',
  (SELECT COUNT(*) FROM circulation_rules), ' rules, ',
  (SELECT COUNT(*) FROM categories), ' categories, ',
  (SELECT COUNT(*) FROM books), ' books, ',
  (SELECT COUNT(*) FROM book_items), ' items, ',
  (SELECT COUNT(*) FROM borrow_records), ' borrows, ',
  (SELECT COUNT(*) FROM fines), ' fines, ',
  (SELECT COUNT(*) FROM holds), ' holds'
) AS result;
