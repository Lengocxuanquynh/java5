package com.fpoly.java5demo.components;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fpoly.java5demo.entities.CartDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.ProductServices;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class CartSessionListener implements HttpSessionListener {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private ProductServices productServices;

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		try {
			session.setAttribute("cart", "{}");
			session.setAttribute("cartCount", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();

		try {
			String userId = null;

			// Lấy userId trực tiếp từ session attributes
			Object userAttr = null;
			try {
				userAttr = session.getAttribute("user");
			} catch (Exception ignored) {
			}

			if (userAttr instanceof User userObj) {
				userId = String.valueOf(userObj.getId());
			} else {
				try {
					Object uidAttr = session.getAttribute("userId");
					if (uidAttr != null) {
						userId = String.valueOf(uidAttr);
					}
				} catch (Exception ignored) {
				}
			}

			if (userId == null) {
				return;
			}

			User user = userJPA.findById(Integer.parseInt(userId)).orElse(null);
			if (user == null) {
				return;
			}

			String cartJson = null;
			try {
				cartJson = (String) session.getAttribute("cart");
			} catch (Exception ignored) {
			}

			if (cartJson != null && !cartJson.isEmpty()) {
				Map<Integer, Integer> cartMap = objectMapper.readValue(cartJson,
						new TypeReference<Map<Integer, Integer>>() {
						});

				List<CartDetail> cartDetails = new ArrayList<>();
				for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
					int prodId = entry.getKey();
					int quantity = entry.getValue();

					Product product = productServices.findById(prodId);
					if (product == null) {
						continue;
					}

					CartDetail cartDetail = new CartDetail();
					cartDetail.setProduct(product);
					cartDetail.setUser(user);
					cartDetail.setQuantity(quantity);
					cartDetails.add(cartDetail);
				}

				cartDetailServices.syncSessionCartToDatabase(cartDetails);
			}
		} catch (Exception e) {
			System.err.println("Lỗi đồng bộ giỏ hàng khi hủy session: " + e.getMessage());
		}
	}
}
