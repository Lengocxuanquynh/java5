# 📜 BẢN QUY TẮC PHONG CÁCH CODE CHUẨN THẦY (JAVA 5 - SD21201)

> **Mục tiêu:** Đồng bộ phong cách lập trình cho toàn bộ thành viên trong nhóm và AI Assistant.  
> **Nguyên tắc cốt lõi:** Tuân thủ triệt để **"phong cách code mộc mạc, cơ bản của thầy"** (dựa trên project mẫu gốc `C:\Users\vinh\eclipse-workspace\Java5-SD21201`), dễ hiểu, dễ chấm điểm, đúng chuẩn bài giảng môn Java 5 FPT Polytechnic.

---

## 📁 1. THƯ MỤC THAM CHIẾU GỐC CỦA THẦY TRÊN MÁY TÍNH
Khi cần đối chiếu trực tiếp cách thầy code từng tính năng, hãy mở các file sau tại `C:\Users\vinh\eclipse-workspace\Java5-SD21201`:
- **Controller:** `.../controllers/UserOrderController.java`, `UserCartController.java`
- **Service:** `.../services/OrderServices.java`
- **JPA:** `.../jpas/CartItemJPA.java`, `UserJPA.java`, `OrderJPA.java`

---

## 🚫 2. BẢNG ĐỐI CHIẾU "NÊN VIẾT" VS "KHÔNG ĐƯỢC VIẾT"

| TIÊU CHÍ | ❌ KHÔNG ĐƯỢC VIẾT (Bị trừ điểm / Khó chấm) | ✅ BẮT BUỘC VIẾT THEO THẦY (Đạt điểm tối đa) |
| :--- | :--- | :--- |
| **Tính toán / Duyệt mảng** | Dùng Stream API: `items.stream().mapToInt(...).sum()` | Dùng vòng lặp for cơ bản: `for(CartItemBean i : items) { total += i.getPrice(); }` |
| **Truy vấn JPA** | Tên hàm dài ngoằng: `findByUserIdAndStatusOrderByDateDesc(...)` | Dùng SQL thuần túy: `@Query(value = "SELECT * FROM orders WHERE ... = ?1", nativeQuery = true)` |
| **Lấy danh tính User** | Dùng Spring Security / SecurityContextHolder / Custom Session phức tạp | Dùng Cookie cơ bản: `Utils.getCookieValue("user_id", request)` |
| **Bắt lỗi nghiệp vụ** | Trả về chuỗi thông báo lằng nhằng hoặc Optional rỗng | Dùng `if (!optional.isPresent()) throw new RuntimeException("Lỗi...")` |
| **Điều hướng Controller** | Cấu hình filter chuyển hướng ngầm | Dùng `try { ... } catch (Exception e) { e.printStackTrace(); return "redirect:..."; }` |
| **Thông tin giao hàng** | Gộp tên, sđt, địa chỉ thành 1 chuỗi dài | Tách riêng 4 cột độc lập: `shippingName`, `shippingPhone`, `shippingAddress`, `note` |
| **Cấu hình Git** | Push đè `application.properties` | Giữ nguyên file local (Mac dùng Docker `StrongPass@123`, Windows dùng pass riêng) |

---

## 💻 3. KHUÔN MẪU CODE THỰC CHIẾN TỪNG TẦNG

### 3.1. Tầng JPA Repository: 100% `@Query(nativeQuery = true)`
```java
public interface OrderJPA extends JpaRepository<Order, Integer> {
    // Luôn dùng cú pháp SQL thuần với tham số ?1, ?2
    @Query(value = "SELECT * FROM orders WHERE user_id = ?1 ORDER BY date DESC, id DESC", nativeQuery = true)
    List<Order> findByUserId(int userId);

    @Query(value = "SELECT * FROM orders WHERE status = ?1 ORDER BY date DESC, id DESC", nativeQuery = true)
    List<Order> findByStatus(int status);
}
```

### 3.2. Tầng Service: Cookie, Optional, throw RuntimeException, Vòng lặp for
```java
@Service
public class OrderServices {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OrderJPA orderJPA;
    @Autowired
    private UserJPA userJPA;

    public int createOrder(CheckoutBean bean) {
        // 1. Lấy userId trực tiếp từ Cookie
        int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
        Optional<User> userOptional = userJPA.findById(userId);

        // 2. Kiểm tra bằng isPresent() và ném RuntimeException
        if (!userOptional.isPresent()) {
            throw new RuntimeException("Tài khoản không tồn tại!");
        }
        User user = userOptional.get();

        // 3. Tính tiền bằng vòng lặp for đơn giản
        int totalAmount = 0;
        for (CartItemBean item : cartItems) {
            totalAmount += item.getQuantity() * item.getPrice();
        }

        // 4. Tạo Entity bằng setter tường minh
        Order order = new Order();
        order.setUser(user);
        order.setShippingName(bean.getFullName());
        order.setShippingPhone(bean.getPhone());
        order.setShippingAddress(bean.getAddress());
        order.setNote(bean.getNote());
        order.setTotalPrice(totalAmount);
        order.setStatus(1); // 1: Chờ xác nhận

        Order saved = orderJPA.save(order);
        return saved.getId();
    }
}
```

### 3.3. Tầng Controller: `@Valid @ModelAttribute`, `Errors errors`, `try-catch`
```java
@Controller
public class OrderController {
    @Autowired
    private OrderServices orderServices;

    @PostMapping("/checkout")
    public String handleOrder(@Valid @ModelAttribute("checkoutBean") CheckoutBean bean,
                             Errors errors, Model model) {
        // 1. Kiểm tra lỗi form đầu tiên
        if (errors.hasErrors()) {
            return "checkout";
        }

        // 2. Bọc toàn bộ trong try-catch, có lỗi thì in log và redirect
        try {
            int orderId = orderServices.createOrder(bean);
            return "redirect:/order/success/" + orderId;
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi để debug
            return "redirect:/cart?error=" + e.getMessage();
        }
    }
}
```

---

## 👥 4. QUY TẮC HỢP TÁC NHÓM & GIT
1. **Comment rõ ràng:** Khi chỉnh sửa vào các module của thành viên khác (User Profile, Quản lý người dùng Admin): **Bắt buộc thêm ghi chú `[Vinh]`** trước các dòng code để cả nhóm biết.
2. **Cơ sở dữ liệu:** Mọi cập nhật bảng hoặc cột phải được ghi vào file `database_shopee.sql` và thông báo cho cả nhóm chạy lại trong SQL Server.
3. **Môi trường chạy:** Không bao giờ push file `application.properties` để tránh xung đột mật khẩu DB giữa Windows và macOS (Docker).
