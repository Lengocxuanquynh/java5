package com.fpoly.java5demo.components;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// [AuthInterceptor] Chặn và kiểm tra quyền tập trung cho CẢ 2 BÊN (User & Admin)
// Chuẩn bài giảng thầy (Java5-SD21201): Dùng Cookie + truy vấn trực tiếp DB, không dùng Session
@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private UserJPA userJPA;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String uri = request.getRequestURI();

		try {
			// 1. Lấy userId từ Cookie (không phụ thuộc Session)
			String userIdStr = Utils.getCookieValue(Utils.COOKIE_KEY_USER_ID, request);
			if (userIdStr == null || userIdStr.trim().isEmpty()) {
				throw new RuntimeException("Chưa đăng nhập - không tìm thấy cookie user_id");
			}

			int userId = Integer.parseInt(userIdStr.trim());

			// 2. Kiểm tra trực tiếp vào Database xem user có tồn tại và đang hoạt động không
			Optional<User> userOptional = userJPA.findById(userId);
			if (!userOptional.isPresent()) {
				throw new RuntimeException("Tài khoản không tồn tại trong DB");
			}

			User user = userOptional.get();
			if (!user.isStatus()) {
				throw new RuntimeException("Tài khoản đã bị khóa");
			}

			// 3. Phía ADMIN: Nếu truy cập các URL quản trị (/admin/**)
			if (uri.startsWith("/admin")) {
				if (user.getRole() != 1) { // 1: Admin
					response.sendRedirect("/login");
					return false;
				}
			}

			// 4. Phía USER: Tài khoản Admin không được phép mua hàng (checkout, thêm vào giỏ)
			if ((uri.startsWith("/checkout") || uri.startsWith("/cart/add")) && user.getRole() == 1) {
				response.sendRedirect("/cart?error=admin_cannot_buy");
				return false;
			}

			// 5. Lưu thông tin user vào request attribute để các Controller và View dùng trực tiếp
			request.setAttribute("currentUser", user);

		} catch (Exception e) {
			// Bất kỳ lỗi nào (chưa có cookie, cookie giả mạo, ID không có trong DB, tài khoản bị khóa)
			// => Ngắt ngay lập tức TRƯỚC KHI Controller và Service kịp chạy
			if (uri.startsWith("/admin")) {
				response.sendRedirect("/login");
			} else {
				String redirectUri = uri;
				if (uri.startsWith("/cart/add")) {
					String referer = request.getHeader("Referer");
					if (referer != null && referer.contains("/")) {
						try {
							java.net.URI ref = new java.net.URI(referer);
							redirectUri = ref.getPath();
							if (ref.getQuery() != null) {
								redirectUri += "?" + ref.getQuery();
							}
						} catch (Exception ignored) {
							redirectUri = "/cart";
						}
					} else {
						redirectUri = "/cart";
					}
				}
				response.sendRedirect("/login?redirect=" + redirectUri);
			}
			return false;
		}

		return true; // Hợp lệ -> Cho phép đi tiếp vào Controller
	}
}
