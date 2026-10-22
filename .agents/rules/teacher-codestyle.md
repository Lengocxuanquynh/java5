---
description: Quy tắc bắt buộc tuân thủ phong cách code mộc mạc cơ bản của thầy môn Java 5 (Java5-SD21201)
globs: ["**/*.java", "**/*.html", "**/*.sql"]
---

# QUY TẮC BẮT BUỘC: PHONG CÁCH CODE MỘC MẠC CỦA THẦY (JAVA 5)

Mọi thay đổi mã nguồn trong dự án này BẮT BUỘC phải tuân thủ nghiêm ngặt các quy tắc sau:

1. **JPA Repository**:
   - KHÔNG dùng method name derivation dài dòng (e.g., `findByUserIdAndStatusOrderByDateDesc`).
   - Mọi truy vấn tùy chỉnh PHẢI dùng `@Query(value = "SELECT * FROM ... WHERE ... = ?1", nativeQuery = true)` với tham số vị trí `?1`, `?2`.
   - Trả về `Optional<Entity>` cho các hàm tìm kiếm bản ghi đơn.

2. **Service Layer**:
   - Dùng Cookie cơ bản: `Utils.getCookieValue("user_id", request)` qua `HttpServletRequest` để xác thực `userId`.
   - Kiểm tra điều kiện bằng `Optional` và ném `throw new RuntimeException("thông báo")` khi có lỗi nghiệp vụ.
   - Dùng vòng lặp `for` truyền thống đơn giản để duyệt và tính toán (`totalPrice += buyQty * price`). TUYỆT ĐỐI KHÔNG dùng Stream API phức tạp (`.stream().mapToInt(...)`).

3. **Controller Layer**:
   - Bọc toàn bộ các action trong khối `try { ... } catch (Exception e) { e.printStackTrace(); return "redirect:..."; }`.
   - Dùng `@Valid @ModelAttribute("bean") ...Bean bean, Errors errors`. Luôn kiểm tra `if (errors.hasErrors())` đầu tiên.

4. **Entity & Beans**:
   - Tách biệt rõ ràng các trường dữ liệu (ví dụ Order: `shippingName`, `shippingPhone`, `shippingAddress`, `note`).
   - Ghi chú `[Vinh]` rõ ràng khi chỉnh sửa code liên quan đến module của thành viên khác (User, Profile, Admin User).

5. **Cấu hình & Git**:
   - KHÔNG commit hay push file `application.properties` lên Git để tránh ghi đè mật khẩu SQL Server của thành viên dùng MacBook (Docker) và Windows.
