package com.fpoly.java5demo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fpoly.java5demo.beans.ForgotPasswordBean;
import com.fpoly.java5demo.beans.RegisterBean;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class UserServices {

	@Autowired
	private UserJPA userJPA;

	public String register(RegisterBean bean) {
		if (userJPA.existsByUsername(bean.getUsername())) {
			return "Tên tài khoản đã tồn tại!";
		}
		if (userJPA.existsByEmail(bean.getEmail())) {
			return "Email đã được sử dụng!";
		}
		// [Vinh] Kiểm tra trùng số điện thoại - phục vụ luồng Order
		if (bean.getPhone() != null && !bean.getPhone().trim().isEmpty()
				&& userJPA.existsByPhone(bean.getPhone().trim())) {
			return "Số điện thoại đã được sử dụng!";
		}

		User user = new User();
		user.setUsername(bean.getUsername());
		user.setPassword(bean.getPassword());
		user.setName(bean.getName());
		user.setEmail(bean.getEmail());
		user.setPhone(bean.getPhone());
		user.setRole(0); // 0: User, 1: Admin
		user.setStatus(true);

		userJPA.save(user);
		return null;
	}

	public String resetPassword(ForgotPasswordBean bean) {
		Optional<User> optionalUser = userJPA.findByUsername(bean.getUsername().trim());
		if (optionalUser.isEmpty()) {
			return "Tên đăng nhập không tồn tại trong hệ thống!";
		}

		User user = optionalUser.get();
		if (!user.getEmail().equalsIgnoreCase(bean.getEmail().trim())) {
			return "Email không khớp với tài khoản đã đăng ký!";
		}

		if (!bean.getNewPassword().equals(bean.getConfirmPassword())) {
			return "Xác nhận mật khẩu mới không trùng khớp!";
		}

		user.setPassword(bean.getNewPassword());
		userJPA.save(user);
		return null;
	}

	public User login(String username, String password, HttpServletResponse response, HttpSession session) {
		User user = login(username, password, session);
		if (user != null && response != null) {
			Utils.setCookie(Utils.COOKIE_KEY_USER_ID, String.valueOf(user.getId()), response);
			Utils.setCookie(Utils.COOKIE_KEY_ROLE, String.valueOf(user.getRole()), response);
			Utils.setCookie("username", user.getUsername(), response);
		}
		return user;
	}

	public User login(String username, String password, HttpSession session) {
		Optional<User> optionalUser = userJPA.findByUsernameAndPassword(username, password);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!user.isStatus()) {
				return null; // Tài khoản bị khóa
			}

			// Lưu vào session
			session.setAttribute("user", user);
			session.setAttribute("userId", user.getId());
			session.setAttribute("role", user.getRole());

			return user;
		}
		return null;
	}

	public void logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		if (session != null) {
			try {
				session.invalidate();
			} catch (Exception ignored) {
			}
		}
		if (response != null) {
			Utils.clearCookie(request, response);
		}
	}

	public User getUserInfoById(int id) {
		return userJPA.findById(id).orElse(null);
	}

	public User getLoggedInUser(HttpServletRequest request, HttpSession session) {
		if (session != null && session.getAttribute("user") != null) {
			return (User) session.getAttribute("user");
		}
		if (request != null) {
			String userIdStr = Utils.getCookieValue(Utils.COOKIE_KEY_USER_ID, request);
			if (userIdStr != null && !userIdStr.trim().isEmpty()) {
				try {
					int userId = Integer.parseInt(userIdStr.trim());
					User user = userJPA.findById(userId).orElse(null);
					if (user != null && user.isStatus()) {
						if (session != null) {
							session.setAttribute("user", user);
							session.setAttribute("userId", user.getId());
							session.setAttribute("role", user.getRole());
						}
						return user;
					}
				} catch (Exception ignored) {
				}
			}
		}
		return null;
	}

	public List<User> getAllUsers() {
		return userJPA.findAll();
	}

	public User findByEmail(String email) {
		return userJPA.findByEmail(email).orElse(null);
	}

	// [Vinh] Tìm user theo SĐT - phục vụ kiểm tra trùng khi cập nhật profile / admin quản lý user
	public User findByPhone(String phone) {
		return userJPA.findByPhone(phone).orElse(null);
	}

	public User save(User user) {
		return userJPA.save(user);
	}

	public void deleteById(int id) {
		userJPA.deleteById(id);
	}
}
