# 📜 QUY TẮC PHONG CÁCH CODE DỰ ÁN JAVA 5 (CODESTYLE RULE)
> **Mục tiêu:** Đồng bộ phong cách lập trình cho toàn bộ thành viên trong nhóm và AI Assistant.  
> **Nguyên tắc cốt lõi:** Tuân thủ triệt để **"phong cách code mộc mạc, cơ bản của thầy"** (dựa trên project mẫu `Java5-SD21201`), dễ hiểu, dễ bảo trì, đúng chuẩn bài giảng môn Java 5 FPT Polytechnic.

---

## 🎯 1. Triết lý chung (Core Philosophy)
- **Ưu tiên sự đơn giản & trực quan:** Viết code theo cú pháp căn bản của Java, người đọc code (kể cả thầy chấm bài) nhìn vào là hiểu ngay luồng chạy.
- **KHÔNG dùng tính năng nâng cao không cần thiết:**
  - ❌ Tránh lạm dụng Java 8 Stream API (`.stream().mapToInt().filter().collect()...`). Thay vào đó, hãy dùng vòng lặp `for` truyền thống.
  - ❌ Tránh dùng các thư viện bảo mật phức tạp (Spring Security, JWT) khi thầy chưa yêu cầu.
  - ❌ Không cấu hình các Annotation hay Framework trung gian khó debug.

---

## 🗄️ 2. Quy tắc Tầng JPA (Repository)
- **Chuẩn hóa 100% câu truy vấn tùy chỉnh sang SQL thuần (`nativeQuery = true`):**
  - Mọi câu SELECT/UPDATE tùy biến **bắt buộc** phải viết câu lệnh SQL tường minh với tham số vị trí `?1`, `?2`.
  ```java
  // ✅ CHUẨN CỦA THẦY:
  @Query(value = "SELECT * FROM users WHERE phone = ?1", nativeQuery = true)
  Optional<User> findByPhone(String phone);

  @Query(value = "SELECT * FROM orders WHERE user_id = ?1 ORDER BY date DESC, id DESC", nativeQuery = true)
  List<Order> findByUserId(int userId);

  // ❌ KHÔNG DÙNG: Tên hàm tự sinh dài dòng dễ nhầm lẫn
  // List<Order> findByUserIdAndStatusOrderByDateDesc(int userId, int status);
  ```
- **Giá trị trả về:**
  - Với các hàm tìm kiếm trả về 1 bản ghi duy nhất (ID, Username, Phone, Email), luôn bọc trong `Optional<Entity>`.

---

## ⚙️ 3. Quy tắc Tầng Service (Xử lý nghiệp vụ)

### 3.1. Xác thực người dùng qua Cookie cơ bản
- Lấy trực tiếp `userId` qua Cookie bằng `HttpServletRequest`:
  ```java
  @Autowired
  private HttpServletRequest request;

  int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
  Optional<User> userOptional = userJPA.findById(userId);
  ```

### 3.2. Bắt lỗi nghiệp vụ bằng `Optional` và ném `RuntimeException`
- Khi kiểm tra điều kiện (tồn kho, giỏ hàng trống, tài khoản không hợp lệ), nếu vi phạm thì `throw new RuntimeException("Thông báo lỗi")` trực tiếp để Controller bắt:
  ```java
  if (!userOptional.isPresent()) {
      throw new RuntimeException("Không tìm thấy tài khoản người dùng!");
  }
  ```

### 3.3. Tính toán bằng vòng lặp `for` cơ bản
- Duyệt danh sách và tính tiền / trừ kho bằng vòng lặp `for` truyền thống:
  ```java
  int totalPrice = 0;
  for (Product item : products) {
      int buyQty = cartItems.getOrDefault(item.getId(), 0);
      if (item.getQuantity() < buyQty) {
          throw new RuntimeException("Sản phẩm '" + item.getName() + "' không đủ số lượng trong kho!");
      }
      totalPrice += buyQty * item.getSalePrice();
  }
  ```

---

## 🌐 4. Quy tắc Tầng Controller (Điều hướng & Giao diện)

### 4.1. Bao bọc bằng khối `try-catch`
- Toàn bộ các action xử lý quan trọng (đặt hàng, cập nhật giỏ, đổi mật khẩu) đều phải bọc trong `try-catch`:
  ```java
  @PostMapping("/checkout")
  public String placeOrder(@Valid @ModelAttribute("checkoutBean") CheckoutBean bean, Errors errors, Model model) {
      if (errors.hasErrors()) {
          return "checkout";
      }
      try {
          int orderId = orderServices.createOrder(bean);
          return "redirect:/order/success/" + orderId;
      } catch (Exception e) {
          e.printStackTrace(); // In lỗi để debug
          return "redirect:/cart?error=" + e.getMessage();
      }
  }
  ```

### 4.2. Form Validation
- Nhận dữ liệu từ form luôn dùng `@Valid @ModelAttribute("beanName") Bean bean, Errors errors`.
- Luôn kiểm tra `if (errors.hasErrors())` đầu tiên trước khi xử lý logic.

---

## 📦 5. Quy tắc Entity & CSDL

### 5.1. Tách bạch các trường dữ liệu (Không gộp chuỗi)
- Điển hình là thông tin giao hàng trong `Order`:
  - `shippingName`: Tên người nhận
  - `shippingPhone`: Số điện thoại người nhận
  - `shippingAddress`: Địa chỉ nhận hàng
  - `note`: Ghi chú đơn hàng
- Bảng `users`: Cột `phone` có ràng buộc `UNIQUE` để bắt lỗi trùng lặp; có thêm cột `address` để lưu địa chỉ mặc định của user.

### 5.2. Chú thích code rõ ràng khi chạm vào phần của thành viên khác
- Ví dụ khi cập nhật thêm trường `address` hoặc kiểm tra trùng SĐT trong `UserController` (Profile) hay `AdminUserController`, luôn thêm comment:
  ```java
  // [Vinh] Đồng bộ địa chỉ và SĐT phục vụ luồng Order
  ```

---

## 🛡️ 6. Quy tắc Làm việc nhóm & Git (Rất quan trọng!)

1. **Tuyệt đối KHÔNG commit file `application.properties` lên Git:**
   - Mỗi thành viên có môi trường kết nối SQL Server khác nhau:
     - Bạn dùng **MacBook** chạy SQL Server qua Docker: mật khẩu thường là `StrongPass@123`.
     - Bạn dùng **Windows** cài SQL Server trực tiếp: mật khẩu thường là `123456`, `sa`...
   - Giữ file này ở Local máy mình để chạy, không push đè mật khẩu của người khác.

2. **Quy trình đẩy code (Push code):**
   ```bash
   git add <cac_file_code>   # Tránh add application.properties
   git commit -m "feat/fix: mô tả ngắn gọn nội dung"
   git push origin <ten_nhanh_cua_ban>
   ```
   Tạo **Pull Request (PR)** trên GitHub -> Báo nhóm hoặc kiểm tra không xung đột trước khi Merge vào `main`.

3. **Chạy lại Database:**
   - Bất cứ khi nào file `database_shopee.sql` có cập nhật, cả nhóm cùng mở SQL Server Management Studio (SSMS) và chạy lại file SQL để đồng bộ dữ liệu.
