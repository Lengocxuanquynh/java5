package com.fpoly.java5demo.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private UserServices userServices;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession();
		User user = userServices.getLoggedInUser(request, session);
		String uri = request.getRequestURI();

		// Nếu truy cập trang admin
		if (uri.startsWith("/admin")) {
			if (user == null) {
				response.sendRedirect("/login?redirect=" + uri);
				return false;
			}
			if (user.getRole() != 1) { // 1: Admin
				response.sendRedirect("/?error=forbidden");
				return false;
			}
		}

		// Nếu truy cập các trang yêu cầu đăng nhập: user profile, order history, checkout, cart
		if (uri.startsWith("/user") || uri.startsWith("/checkout") || uri.startsWith("/cart")) {
			if (user == null) {
				response.sendRedirect("/login?redirect=" + uri);
				return false;
			}
		}

		// Tài khoản Quản trị viên (Admin) không được phép thực hiện mua hàng (checkout)
		if (uri.startsWith("/checkout") && user != null && user.getRole() == 1) {
			response.sendRedirect("/cart?error=admin_cannot_buy");
			return false;
		}

		return true;
	}
}
