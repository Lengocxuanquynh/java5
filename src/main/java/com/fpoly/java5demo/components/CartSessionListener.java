package com.fpoly.java5demo.components;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

// [CartSessionListener] Đã vô hiệu hóa vì giỏ hàng lưu trực tiếp vào Database theo user_id (Cookie)
// Không còn phụ thuộc vào HttpSession theo chuẩn style thầy (Java5-SD21201)
public class CartSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
	}
}
