# Shopee Clone - Dự Án Java 5 (Spring Boot & JPA)

Dự án website thương mại điện tử **Shopee Clone** cơ bản được xây dựng dựa trên nền tảng **Spring Boot**, **Spring Data JPA**, **Thymeleaf**, **Bootstrap 5** và **Microsoft SQL Server**, tuân thủ 100% cấu trúc chuẩn môn học **Java 5 (FPT Polytechnic)**.

> 📚 **Tài liệu & Giáo trình chuyên sâu**:
> - Xem giáo trình toàn tập lý thuyết, code mẫu, kết quả và ví dụ thực tế: [GIAO_TRINH_TOAN_TAP_JAVA5_SPRINGBOOT_SHOPEE.md](file:///Users/chloe/Documents/java5/Java5-main/GIAO_TRINH_TOAN_TAP_JAVA5_SPRINGBOOT_SHOPEE.md)
> - Xem cẩm nang tra cứu và kịch bản nghiệp vụ: [TAI_LIEU_JAVA5_SHOPEE.md](file:///Users/chloe/Documents/java5/Java5-main/TAI_LIEU_JAVA5_SHOPEE.md)

---

## 1. Cấu Trúc Dự Án (Project Structure)

```text
Java5-main/
├── src/main/java/com/fpoly/java5demo/
│   ├── config/
│   │   └── AuthConfig.java              # Cấu hình Interceptor & static upload resources
│   ├── components/
│   │   ├── AuthInterceptor.java         # Interceptor kiểm tra đăng nhập & phân quyền role
│   │   ├── CartSessionListener.java     # Quản lý session giỏ hàng & đồng bộ với Database
│   │   └── TestCronJobs.java            # Hẹn giờ Scheduled / Cron jobs
│   ├── entities/
│   │   ├── User.java                    # Entity người dùng (id, username, password, role, status)
│   │   ├── Category.java                # Entity danh mục (id, name, products)
│   │   ├── Product.java                 # Entity sản phẩm (id, name, price, quantity, status, category, images)
│   │   ├── Image.java                   # Entity hình ảnh sản phẩm
│   │   ├── CartDetail.java              # Entity giỏ hàng lưu DB (user, product, quantity)
│   │   ├── Order.java                   # Entity đơn hàng (id, address, totalPrice, date, status, user)
│   │   └── OrderDetail.java             # Entity chi tiết đơn hàng (price, quantity, order, product)
│   ├── beans/
│   │   ├── LoginBean.java               # Form bean đăng nhập
│   │   ├── RegisterBean.java            # Form bean đăng ký tài khoản
│   │   ├── ProductFormBean.java         # Form bean thêm/sửa sản phẩm + upload ảnh
│   │   ├── CategoryFormBean.java        # Form bean danh mục
│   │   ├── CheckoutBean.java            # Form bean thanh toán đơn hàng
│   │   └── CartItemBean.java            # Bean hiển thị giỏ hàng
│   ├── jpas/
│   │   ├── UserJPA.java                 # Truy vấn User (tìm username, email, xác thực)
│   │   ├── CategoryJPA.java             # Truy vấn Category
│   │   ├── ProductJPA.java              # Lọc sản phẩm theo danh mục, từ khóa, trạng thái
│   │   ├── ImageJPA.java                # Lưu và lấy ảnh theo productId
│   │   ├── CartDetailJPA.java           # Quản lý giỏ hàng DB
│   │   ├── OrderJPA.java                # Quản lý đơn hàng (theo user, theo trạng thái, sắp xếp)
│   │   └── OrderDetailJPA.java          # Lấy chi tiết món hàng theo mã đơn
│   ├── services/
│   │   ├── UserServices.java            # Đăng nhập, đăng ký, phiên đăng nhập, cookie
│   │   ├── ProductServices.java         # CRUD sản phẩm, upload ảnh, tìm kiếm
│   │   ├── CategoryServices.java        # CRUD danh mục
│   │   ├── CartDetailServices.java      # Thêm/sửa/xóa giỏ hàng Session & DB, tính tổng tiền
│   │   ├── OrderServices.java           # Đặt hàng, trừ kho, hủy đơn, cập nhật trạng thái
│   │   └── ImageServices.java           # Xử lý lưu file ảnh vào thư mục uploads/
│   ├── controllers/
│   │   ├── HomeController.java          # Trang chủ Shopee, lọc danh mục, tìm kiếm, chi tiết SP
│   │   ├── AuthController.java          # Đăng nhập, đăng ký, đăng xuất
│   │   ├── CartController.java          # Xem giỏ hàng, thêm giỏ, cập nhật số lượng, xóa
│   │   ├── OrderController.java         # Thanh toán, hóa đơn thành công, lịch sử đơn mua
│   │   ├── ProductController.java       # Quản trị Admin: Dashboard & CRUD Sản phẩm
│   │   ├── CategoryController.java      # Quản trị Admin: CRUD Danh mục
│   │   └── AdminOrderController.java    # Quản trị Admin: Quản lý & duyệt đơn hàng
│   ├── utils/
│   │   └── Utils.java                   # Hỗ trợ xử lý Cookie và định dạng dữ liệu
│   └── Java5Application.java           # Điểm khởi chạy ứng dụng Spring Boot
│
├── src/main/resources/
│   ├── static/
│   │   └── css/
│   │       └── shopee.css               # Giao diện chuẩn phong cách Shopee (Màu cam #ee4d2d)
│   ├── templates/
│   │   ├── fragments/
│   │   │   ├── navbar.html              # Thanh menu, tìm kiếm, giỏ hàng, thông tin tài khoản
│   │   │   └── footer.html              # Footer phong cách Shopee
│   │   ├── home.html                    # Trang chủ (Banner, Danh mục, Gợi ý hôm nay)
│   │   ├── product-detail.html          # Chi tiết sản phẩm (Ảnh, giá, số lượng, Thêm giỏ/Mua ngay)
│   │   ├── cart.html                    # Giỏ hàng Shopee (Tăng giảm SL, tổng tiền, mua hàng)
│   │   ├── checkout.html                # Thanh toán (Địa chỉ, sản phẩm, phương thức thanh toán)
│   │   ├── order-success.html           # Thông báo đặt hàng thành công
│   │   ├── login.html                   # Đăng nhập
│   │   ├── register.html                # Đăng ký
│   │   ├── user/
│   │   │   ├── orders.html              # Lịch sử đơn mua của tôi + Hủy đơn hàng
│   │   │   └── order-detail.html        # Chi tiết đơn hàng người dùng
│   │   └── admin/
│   │       ├── dashboard.html           # Bảng điều khiển quản trị viên
│   │       ├── product-form.html        # Quản lý sản phẩm (Thêm/Sửa/Xóa + Upload ảnh)
│   │       ├── category-form.html       # Quản lý danh mục
│   │       ├── order-list.html          # Danh sách đơn hàng toàn sàn
│   │       └── order-detail.html        # Chi tiết đơn hàng & cập nhật trạng thái
│   └── application.properties           # Cấu hình kết nối SQL Server
│
├── database_shopee.sql                  # Script tạo Database & dữ liệu mẫu SQL Server
└── uploads/                             # Thư mục lưu hình ảnh sản phẩm tải lên
```

---

## 2. Hướng Dẫn Cài Đặt Cơ Sở Dữ Liệu (Database Setup)

1. Mở công cụ **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Mở file [database_shopee.sql](file:///Users/chloe/Documents/java5/Java5-main/database_shopee.sql) và nhấn **Execute (F5)** để tự động:
   - Tạo CSDL `java5`
   - Tạo các bảng: `users`, `categories`, `products`, `images`, `cart_details`, `orders`, `order_details`
   - Chèn dữ liệu mẫu đầy đủ các sản phẩm hot, danh mục, đơn hàng mẫu.
3. Kiểm tra thông tin kết nối trong file [application.properties](file:///Users/chloe/Documents/java5/Java5-main/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=java5;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=123456aA@
   ```
   *(Thay đổi username/password nếu SQL Server của bạn khác mật khẩu trên)*.

---

## 3. Tài Khoản Đăng Nhập Mẫu

| Vai trò | Tên đăng nhập | Mật khẩu | Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `123456` | Toàn quyền vào trang Quản trị (`/admin`), quản lý sản phẩm, danh mục, đơn hàng |
| **Khách hàng (User)** | `user1` | `123456` | Mua sắm, giỏ hàng, thanh toán, xem lịch sử đơn mua (`/user/orders`) |
| **Khách hàng 2** | `user2` | `123456` | Tài khoản khách hàng mẫu thứ 2 |

---

## 4. Các Chức Năng Nổi Bật

### 4.1. Phía Người Dùng (Khách Hàng)
- **Trang chủ Shopee (`/`)**:
  - Banner quảng cáo trượt, thanh danh mục sản phẩm tương tác nhanh.
  - Tìm kiếm sản phẩm theo từ khóa (`/search?keyword=...`).
  - Lọc sản phẩm theo danh mục (`/category/{id}`).
  - Lưới sản phẩm chuẩn Shopee: Huy hiệu Yêu thích, giảm giá, số lượng tồn kho.
- **Trang chi tiết sản phẩm (`/product/{id}`)**:
  - Bộ sưu tập ảnh sản phẩm, đánh giá sao, số lượng đã bán.
  - Bộ chọn số lượng có kiểm tra giới hạn tồn kho.
  - 2 tùy chọn mua: **"Thêm Vào Giỏ Hàng"** hoặc **"Mua Ngay"**.
  - Khối sản phẩm tương tự cùng danh mục.
- **Giỏ hàng (`/cart`)**:
  - Quản lý sản phẩm bằng Session kết hợp đồng bộ Database (`CartSessionListener`).
  - Tăng/giảm số lượng trực tiếp trên từng món hàng.
  - Xóa từng món hoặc xóa sạch toàn bộ giỏ hàng.
  - Thanh tổng tiền thanh toán cố định.
- **Đặt hàng & Thanh toán (`/checkout`)**:
  - Nhập địa chỉ nhận hàng, họ tên, số điện thoại người nhận.
  - Chọn phương thức thanh toán: COD, Ví ShopeePay, Chuyển khoản ngân hàng.
  - Kiểm tra tồn kho trước khi đặt hàng, tự động trừ số lượng sản phẩm trong kho.
- **Lịch sử đơn mua (`/user/orders`)**:
  - Xem danh sách đơn hàng đã mua với các trạng thái rõ ràng.
  - Xem chi tiết từng đơn hàng (`/user/order/{id}`).
  - Cho phép người dùng **Hủy đơn hàng** khi đơn hàng còn ở trạng thái *Chờ xác nhận*. Tự động hoàn lại số lượng tồn kho của sản phẩm khi hủy.

### 4.2. Phía Quản Trị Viên (Admin)
- **Bảng điều khiển (`/admin` hoặc `/admin/dashboard`)**:
  - Thống kê tổng số sản phẩm, danh mục, đơn hàng.
- **Quản lý sản phẩm (`/admin/product-form`)**:
  - Thêm mới sản phẩm có hỗ trợ **tải lên nhiều hình ảnh (MultipartFile)** vào thư mục `uploads/`.
  - Cập nhật thông tin và chỉnh sửa hình ảnh sản phẩm.
  - Xóa / Ẩn sản phẩm.
- **Quản lý danh mục (`/admin/category-form`)**:
  - Thêm mới, chỉnh sửa và xóa danh mục sản phẩm (có kiểm tra trùng tên danh mục).
- **Quản lý đơn hàng (`/admin/orders`)**:
  - Xem toàn bộ danh sách đơn hàng của khách hàng trên toàn sàn.
  - Cập nhật tiến độ đơn hàng: *Chờ xác nhận -> Đã xác nhận -> Đang giao hàng -> Đã nhận hàng -> Hoàn thành / Hủy*.

### 4.3. Bảo Mật & Phân Quyền
- Sử dụng **[AuthInterceptor](file:///Users/chloe/Documents/java5/Java5-main/src/main/java/com/fpoly/java5demo/components/AuthInterceptor.java)** đăng ký qua **[AuthConfig](file:///Users/chloe/Documents/java5/Java5-main/src/main/java/com/fpoly/java5demo/config/AuthConfig.java)**:
  - Chặn người lạ truy cập vào trang quản trị (`/admin/**`) nếu không phải tài khoản Admin (`role = 1`).
  - Tự động chuyển hướng về trang `/login` nếu truy cập vào các trang yêu cầu đăng nhập (`/checkout`, `/user/**`).
# java5
