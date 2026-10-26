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

@Service
public class UserServices {

	@Autowired
	private UserJPA userJPA;

	public void register(RegisterBean bean) {
		if (userJPA.countByUsername(bean.getUsername()) > 0) {
			throw new RuntimeException("Tên tài khoản đã tồn tại!");
		}
		if (userJPA.countByEmail(bean.getEmail()) > 0) {
			throw new RuntimeException("Email đã được sử dụng!");
		}
		// [Vinh] Kiểm tra trùng số điện thoại - phục vụ luồng Order
		if (bean.getPhone() != null && !bean.getPhone().trim().isEmpty()
				&& userJPA.countByPhone(bean.getPhone().trim()) > 0) {
			throw new RuntimeException("Số điện thoại đã được sử dụng!");
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
	}

	public void resetPassword(ForgotPasswordBean bean) {
		Optional<User> optionalUser = userJPA.findByUsername(bean.getUsername().trim());
		if (!optionalUser.isPresent()) {
			throw new RuntimeException("Tên đăng nhập không tồn tại trong hệ thống!");
		}

		User user = optionalUser.get();
		if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(bean.getEmail().trim())) {
			throw new RuntimeException("Email không khớp với tài khoản đã đăng ký!");
		}

		if (!bean.getNewPassword().equals(bean.getConfirmPassword())) {
			throw new RuntimeException("Xác nhận mật khẩu mới không trùng khớp!");
		}

		user.setPassword(bean.getNewPassword());
		userJPA.save(user);
	}

	public User login(String username, String password, HttpServletResponse response) {
		Optional<User> optionalUser = userJPA.findByUsernameAndPassword(username, password);
		if (!optionalUser.isPresent()) {
			throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác!");
		}

		User user = optionalUser.get();
		if (!user.isStatus()) {
			throw new RuntimeException("Tài khoản đã bị khóa!");
		}

		// Lưu Cookie thuần túy theo chuẩn thầy (không lưu vào Session)
		if (response != null) {
			Utils.setCookie(Utils.COOKIE_KEY_USER_ID, String.valueOf(user.getId()), response);
			Utils.setCookie(Utils.COOKIE_KEY_ROLE, String.valueOf(user.getRole()), response);
			Utils.setCookie("username", user.getUsername(), response);
		}
		return user;
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		if (response != null) {
			Utils.clearCookie(request, response);
		}
	}

	public User getUserInfoById(int id) {
		Optional<User> userOptional = userJPA.findById(id);
		if (!userOptional.isPresent()) {
			return null;
		}
		return userOptional.get();
	}

	/**
	 * [Cookie Auth] Lấy user đang đăng nhập dựa hoàn toàn vào Cookie và kiểm tra trực tiếp DB
	 */
	public User getLoggedInUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		// 1. Ưu tiên lấy từ request attribute do AuthInterceptor đã nạp sẵn
		Object cached = request.getAttribute("currentUser");
		if (cached instanceof User) {
			return (User) cached;
		}
		// 2. Đọc Cookie user_id và truy vấn DB
		String userIdStr = Utils.getCookieValue(Utils.COOKIE_KEY_USER_ID, request);
		if (userIdStr != null && !userIdStr.trim().isEmpty()) {
			try {
				int userId = Integer.parseInt(userIdStr.trim());
				Optional<User> userOptional = userJPA.findById(userId);
				if (userOptional.isPresent() && userOptional.get().isStatus()) {
					return userOptional.get();
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	/**
	 * [checkAuth] Hàm kiểm tra đăng nhập & quyền dùng chung cho cả 2 bên (User & Admin)
	 * @param request HttpServletRequest
	 * @param requiredRole null: chỉ cần đăng nhập, 0: bắt buộc User, 1: bắt buộc Admin
	 * @return User nếu hợp lệ, null nếu không hợp lệ / chưa đăng nhập
	 */
	public User checkAuth(HttpServletRequest request, Integer requiredRole) {
		User user = getLoggedInUser(request);
		if (user == null) {
			return null;
		}
		if (requiredRole != null && user.getRole() != requiredRole) {
			return null;
		}
		return user;
	}

	public List<User> getAllUsers() {
		return userJPA.findAllUsers();
	}

	public User findByEmail(String email) {
		Optional<User> userOptional = userJPA.findByEmail(email);
		if (!userOptional.isPresent()) {
			return null;
		}
		return userOptional.get();
	}

	// [Vinh] Tìm user theo SĐT - phục vụ kiểm tra trùng khi cập nhật profile / admin quản lý user
	public User findByPhone(String phone) {
		Optional<User> userOptional = userJPA.findByPhone(phone);
		if (!userOptional.isPresent()) {
			return null;
		}
		return userOptional.get();
	}

	public User save(User user) {
		return userJPA.save(user);
	}

	public void deleteById(int id) {
		userJPA.deleteById(id);
	}
}
