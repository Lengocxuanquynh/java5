package com.fpoly.java5demo.components;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// [AuthInterceptor] Chặn và kiểm tra quyền tập trung cho CẢ 2 BÊN (User & Admin)
@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private UserJPA userJPA;

	// Hàm này sẽ được kích hoạt sau khi user gửi request và trước khi controller thực thi
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String path = request.getServletPath();

		try {
			// 1. Lấy userId từ Cookie (không phụ thuộc Session)
			String userIdStr = Utils.getCookieValue(Utils.COOKIE_KEY_USER_ID, request);
			if (userIdStr == null || userIdStr.trim().isEmpty()) {
				throw new RuntimeException("Chưa đăng nhập");
			}

			int userId = Integer.parseInt(userIdStr.trim());

			// 2. Kiểm tra trực tiếp vào Database xem user có tồn tại và đang hoạt động không
			Optional<User> userOptional = userJPA.findById(userId);
			if (!userOptional.isPresent()) {
				throw new RuntimeException("Tài khoản không tồn tại");
			}

			User user = userOptional.get();
			if (!user.isStatus()) {
				throw new RuntimeException("Tài khoản đã bị khóa");
			}

			// 3. Phía ADMIN: Nếu truy cập các URL quản trị (/admin/**)
			if (path.startsWith("/admin")) {
				if (user.getRole() != 1) { // 1: Admin
					response.sendRedirect("/login");
					return false;
				}
			}

			// 4. Phía USER: Tài khoản Admin không được phép mua hàng (checkout, thêm vào giỏ)
			if ((path.startsWith("/checkout") || path.startsWith("/cart/add")) && user.getRole() == 1) {
				response.sendRedirect("/cart?error=admin_cannot_buy");
				return false;
			}

			// 5. Lưu thông tin user vào request attribute để các Controller và View dùng trực tiếp
			request.setAttribute("currentUser", user);

		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect("/login");
			return false;
		}

		return true; // Hợp lệ -> Cho phép đi tiếp vào Controller
	}

	// Hàm này sẽ được kích hoạt khi controller chạy xong và trước khi render giao diện
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	// Hàm này sẽ được kích hoạt khi html render thành công và trước khi hiển thị lên giao diện
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
