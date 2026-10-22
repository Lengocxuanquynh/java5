-- ==========================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU SHOPEE CLONE (JAVA 5)
-- HỆ QUẢN TRỊ CSDL: MICROSOFT SQL SERVER
-- ==========================================================

-- 1. TẠO DATABASE (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'java5')
BEGIN
    CREATE DATABASE java5;
END
GO

USE java5;
GO

-- 2. XÓA CÁC BẢNG CŨ NẾU ĐÃ TỒN TẠI ĐỂ RESET DỮ LIỆU
IF OBJECT_ID('order_details', 'U') IS NOT NULL DROP TABLE order_details;
IF OBJECT_ID('cart_details', 'U') IS NOT NULL DROP TABLE cart_details;
IF OBJECT_ID('images', 'U') IS NOT NULL DROP TABLE images;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('products', 'U') IS NOT NULL DROP TABLE products;
IF OBJECT_ID('categories', 'U') IS NOT NULL DROP TABLE categories;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
GO

-- 3. TẠO BẢNG USERS (NGƯỜI DÙNG)
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,             -- Không cho trùng số điện thoại
    address NVARCHAR(255),                -- Địa chỉ mặc định của người dùng
    status BIT NOT NULL DEFAULT 1,
    role INT NOT NULL DEFAULT 0           -- 0: User / Khách hàng, 1: Admin / Quản trị viên
);
GO

-- 4. TẠO BẢNG CATEGORIES (DANH MỤC SẢN PHẨM)
CREATE TABLE categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL
);
GO

-- 5. TẠO BẢNG PRODUCTS (SẢN PHẨM)
CREATE TABLE products (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    price INT NOT NULL,
    discount INT NOT NULL DEFAULT 0,
    quantity INT NOT NULL DEFAULT 0,
    status BIT NOT NULL DEFAULT 1,
    description NVARCHAR(MAX),
    detail NVARCHAR(MAX),
    cat_id INT NOT NULL,
    CONSTRAINT FK_Products_Categories FOREIGN KEY (cat_id) REFERENCES categories(id) ON DELETE CASCADE
);
GO

-- 6. TẠO BẢNG IMAGES (HÌNH ẢNH SẢN PHẨM)
CREATE TABLE images (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    prod_id INT NOT NULL,
    CONSTRAINT FK_Images_Products FOREIGN KEY (prod_id) REFERENCES products(id) ON DELETE CASCADE
);
GO

-- 7. TẠO BẢNG CART_DETAILS (CHI TIẾT GIỎ HÀNG)
CREATE TABLE cart_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 1,
    user_id INT NULL,
    prod_id INT NULL,
    CONSTRAINT FK_Cart_Users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_Cart_Products FOREIGN KEY (prod_id) REFERENCES products(id) ON DELETE CASCADE
);
GO

-- 8. TẠO BẢNG ORDERS (ĐƠN HÀNG)
CREATE TABLE orders (
    id INT IDENTITY(1,1) PRIMARY KEY,
    shipping_name NVARCHAR(100) NOT NULL,  -- Tên người nhận hàng
    shipping_phone VARCHAR(20) NOT NULL,   -- SĐT người nhận hàng
    shipping_address NVARCHAR(255) NOT NULL,-- Địa chỉ giao hàng
    note NVARCHAR(500),                    -- Ghi chú cho shipper
    total_price INT NOT NULL DEFAULT 0,
    date DATE NOT NULL,
    status INT NOT NULL DEFAULT 1,         -- 1: Chờ xác nhận, 2: Đã xác nhận, 3: Đang giao, 4: Đã giao, 5: Đã nhận, 6: Hủy
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT FK_Orders_Users FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

-- 9. TẠO BẢNG ORDER_DETAILS (CHI TIẾT ĐƠN HÀNG)
CREATE TABLE order_details (
    id INT IDENTITY(1,1) PRIMARY KEY,
    price INT NOT NULL,
    quantity INT NOT NULL,
    order_id INT NOT NULL,
    prod_id INT NOT NULL,
    CONSTRAINT FK_OrderDetails_Orders FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_Products FOREIGN KEY (prod_id) REFERENCES products(id)
);
GO

-- ==========================================================
-- CHÈN DỮ LIỆU MẪU (SEED DATA)
-- ==========================================================

-- A. Users
INSERT INTO users (username, password, name, email, phone, address, status, role) VALUES 
('admin', '123456', N'Quản Trị Viên Shopee', 'admin@shopee.vn', '0987654321', NULL, 1, 1),
('user1', '123456', N'Nguyễn Văn A', 'nguyenvana@gmail.com', '0912345678', N'Tòa FPT Polytechnic, Phố Trịnh Văn Bô, Nam Từ Liêm, Hà Nội', 1, 0),
('user2', '123456', N'Trần Thị Bích', 'bichthitran@gmail.com', '0988776655', N'123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', 1, 0);
GO

-- B. Categories
INSERT INTO categories (name) VALUES 
(N'Điện Thoại & Phụ Kiện'),
(N'Máy Tính & Laptop'),
(N'Thiết Bị Điện Tử');
GO

-- C. Products
INSERT INTO products (name, price, quantity, status, cat_id) VALUES 
(N'Điện thoại Apple iPhone 15 Pro Max 256GB Titan Tự Nhiên', 29490000, 30, 1, 1),
(N'Tai nghe không dây Bluetooth True Wireless chống ồn chủ động', 350000, 150, 1, 1),
(N'Củ sạc nhanh 20W Type-C chuẩn PD cho iPhone / iPad', 180000, 200, 1, 1),
(N'Laptop Apple MacBook Air M2 13.6 inch 8GB RAM 256GB SSD', 23990000, 15, 1, 2),
(N'Chuột máy tính không dây công thái học Silent Click', 220000, 80, 1, 2),
(N'Bàn phím cơ không dây Bluetooth RGB Led 87 phím Hot-swap', 750000, 50, 1, 2),
(N'Loa Bluetooth không dây mini âm bass siêu trầm chống nước', 290000, 120, 1, 3),
(N'Đồng hồ thông minh Smartwatch theo dõi nhịp tim chống nước IP68', 680000, 60, 1, 3);
GO

-- D. Đơn hàng mẫu
INSERT INTO orders (shipping_name, shipping_phone, shipping_address, note, total_price, date, status, user_id) VALUES 
(N'Nguyễn Văn A', '0912345678', N'Tòa FPT Polytechnic, Phố Trịnh Văn Bô, Nam Từ Liêm, Hà Nội', N'Giao giờ hành chính', 530000, '2026-09-25', 1, 2),
(N'Trần Thị Bích', '0988776655', N'123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh', NULL, 350000, '2026-09-26', 1, 3);
GO

-- E. Chi tiết đơn hàng mẫu
INSERT INTO order_details (price, quantity, order_id, prod_id) VALUES 
(350000, 1, 1, 2),
(180000, 1, 1, 3),
(350000, 1, 2, 2);
GO

PRINT N'Khởi tạo cơ sở dữ liệu Shopee Clone (Java 5) thành công!';