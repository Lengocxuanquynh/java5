---
description: Quy tắc bắt buộc tuân thủ phong cách code mộc mạc cơ bản của thầy môn Java 5 (Java5-SD21201)
globs: ["**/*.java", "**/*.html", "**/*.sql"]
---

# ⚠️ BẮT BUỘC: QUY TẮC PHONG CÁCH CODE MỘC MẠC CỦA THẦY (JAVA 5 - SD21201)

> **DÀNH CHO TẤT CẢ CÁC AI AGENT:**  
> Dự án này là bài tập môn Java 5 (FPT Polytechnic). Thầy giáo chấm điểm dựa trên phong cách lập trình cơ bản, mộc mạc, không dùng các tính năng nâng cao.  
> Thư mục code mẫu gốc của thầy nằm tại: `C:\Users\vinh\eclipse-workspace\Java5-SD21201`.  
> Khi thực hiện bất kỳ tác vụ nào liên quan đến Controller, Service, JPA, Cookie, Đặt hàng, Agent PHẢI tuân thủ các khuôn mẫu (templates) dưới đây:

---

## 📁 1. THƯ MỤC THAM CHIẾU GỐC CỦA THẦY
Khi cần xem trực tiếp cách thầy viết, agent hãy đọc các file sau tại `C:\Users\vinh\eclipse-workspace\Java5-SD21201`:
- Controller: `.../controllers/UserOrderController.java`, `UserCartController.java`
- Service: `.../services/OrderServices.java`
- JPA: `.../jpas/CartItemJPA.java`, `UserJPA.java`, `OrderJPA.java`

---

## 🚫 2. BẢNG CẤM KỴ (ANTI-PATTERNS) - TUYỆT ĐỐI KHÔNG VIẾT

| KHÔNG ĐƯỢC VIẾT (❌ SAI) | BẮT BUỘC VIẾT THEO THẦY (✅ ĐÚNG) |
| :--- | :--- |
| **Stream API:** `list.stream().mapToInt(...).sum()` | **Vòng lặp for cơ bản:** `int sum = 0; for(Item i : list) { sum += i.getPrice(); }` |
| **Query derivation dài dòng:** `findByUserIdAndStatusOrderByDateDesc(...)` | **SQL thuần túy:** `@Query(value = "SELECT * FROM orders WHERE user_id = ?1 AND status = ?2 ORDER BY date DESC", nativeQuery = true)` |
| **Spring Security / Custom Auth Filter** | **Dùng Cookie lấy trực tiếp:** `int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));` |
| **Gộp thông tin giao hàng thành 1 chuỗi** | **Tách 4 trường riêng:** `shippingName`, `shippingPhone`, `shippingAddress`, `note` |
| **Commit `application.properties` lên Git** | **Giữ nguyên file local:** Không push password máy Windows đè lên máy Mac Docker `StrongPass@123` |

---

## 💻 3. KHUÔN MẪU CODE CHUẨN CỦA THẦY (CODE TEMPLATES)

### 3.1. Tầng JPA Repository: 100% `@Query(nativeQuery = true)`
```java
public interface OrderJPA extends JpaRepository<Order, Integer> {
    // Luôn dùng tham số vị trí ?1, ?2
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
        // Kiểm tra lỗi form đầu tiên
        if (errors.hasErrors()) {
            return "checkout";
        }

        // Bọc toàn bộ trong try-catch, ném lỗi thì redirect
        try {
            int orderId = orderServices.createOrder(bean);
            return "redirect:/order/success/" + orderId;
        } catch (Exception e) {
            e.printStackTrace(); // In vết lỗi để debug
            return "redirect:/cart?error=" + e.getMessage();
        }
    }
}
```

---

## 👥 4. QUY TẮC HỢP TÁC NHÓM
- Khi chỉnh sửa vào các module của người khác (User Profile, Quản lý người dùng Admin): **Bắt buộc thêm ghi chú `[Vinh]`** trước các dòng code để thành viên khác trong nhóm nhận biết.
- Không sửa schema DB tùy tiện mà không cập nhật đồng bộ vào file `database_shopee.sql`.
