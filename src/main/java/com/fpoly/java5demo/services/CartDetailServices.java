package com.fpoly.java5demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fpoly.java5demo.beans.CartItemBean;
import com.fpoly.java5demo.entities.CartDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.CartDetailJPA;
import com.fpoly.java5demo.jpas.UserJPA;

import jakarta.servlet.http.HttpSession;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class CartDetailServices {

	@Autowired
	private CartDetailJPA cartDetailJPA;

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private HttpSession session;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductServices productServices;

	@Autowired(required = false)
	private jakarta.servlet.http.HttpServletRequest request;

	public CartDetailServices() {
	}

	public List<CartDetail> getCartsByUserId(int id) {
		List<CartDetail> cartDetails = new ArrayList<>();
		try {
			cartDetails = cartDetailJPA.findByUserId(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cartDetails;
	}

	public boolean syncSessionCartToDatabase(List<CartDetail> cartDetails) {
		try {
			if (cartDetails != null && !cartDetails.isEmpty() && cartDetails.get(0).getUser() != null) {
				cartDetailJPA.deleteByUserId(cartDetails.get(0).getUser().getId());
				cartDetailJPA.saveAll(cartDetails);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void loadCartFromDatabaseToSession(int userId) {
		try {
			List<CartDetail> cartDetails = getCartsByUserId(userId);
			if (cartDetails != null && !cartDetails.isEmpty()) {
				Map<Integer, Integer> cartMap = new HashMap<>();
				for (CartDetail cartDetail : cartDetails) {
					if (cartDetail.getProduct() != null && cartDetail.getProduct().isStatus()) {
						cartMap.put(cartDetail.getProduct().getId(), cartDetail.getQuantity());
					}
				}
				saveCartToSession(cartMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean addToCart(int prodId, int count) {
		Map<Integer, Integer> cartMap = this.getCartMapSession();
		Product product = productServices.findById(prodId);
		if (product == null || !product.isStatus()) {
			return false;
		}

		int currentQty = cartMap.getOrDefault(prodId, 0);
		int newQty = currentQty + count;

		if (product.getQuantity() < newQty) {
			newQty = product.getQuantity();
		}

		if (newQty <= 0) {
			cartMap.remove(prodId);
		} else {
			cartMap.put(prodId, newQty);
		}

		saveCartToSession(cartMap);
		return true;
	}

	public boolean updateQuantity(int prodId, int quantity) {
		Map<Integer, Integer> cartMap = this.getCartMapSession();
		Product product = productServices.findById(prodId);
		if (product == null) {
			return false;
		}

		if (quantity <= 0) {
			cartMap.remove(prodId);
		} else {
			if (quantity > product.getQuantity()) {
				quantity = product.getQuantity();
			}
			cartMap.put(prodId, quantity);
		}

		saveCartToSession(cartMap);
		return true;
	}

	public boolean removeCart(int prodId) {
		Map<Integer, Integer> cartMap = this.getCartMapSession();
		if (cartMap.containsKey(prodId)) {
			cartMap.remove(prodId);
			saveCartToSession(cartMap);
			return true;
		}
		return false;
	}

	public void clearCart() {
		try {
			session.setAttribute("cart", "{}");
			session.setAttribute("cartCount", 0);
			Object uidAttr = session.getAttribute("userId");
			if (uidAttr == null) {
				Object userAttr = session.getAttribute("user");
				if (userAttr instanceof User u) {
					uidAttr = u.getId();
				}
			}
			if (uidAttr == null && request != null) {
				String cookieUid = com.fpoly.java5demo.utils.Utils.getCookieValue(com.fpoly.java5demo.utils.Utils.COOKIE_KEY_USER_ID, request);
				if (cookieUid != null && !cookieUid.trim().isEmpty()) {
					uidAttr = cookieUid.trim();
				}
			}
			if (uidAttr != null) {
				int userId = Integer.parseInt(String.valueOf(uidAttr));
				cartDetailJPA.deleteByUserId(userId);
			}
		} catch (Exception ignored) {
		}
	}

	public void clearCart(int userId) {
		clearCart();
		try {
			cartDetailJPA.deleteByUserId(userId);
		} catch (Exception e) {
			System.err.println("Lỗi xóa giỏ hàng database của user " + userId + ": " + e.getMessage());
		}
	}

	public void removeItemsFromCart(int userId, List<Integer> prodIds) {
		if (prodIds == null || prodIds.isEmpty()) {
			clearCart(userId);
			return;
		}

		Map<Integer, Integer> cartMap = getCartMapSession();
		for (Integer id : prodIds) {
			cartMap.remove(id);
		}
		saveCartToSession(cartMap);

		try {
			cartDetailJPA.deleteByUserIdAndProductIds(userId, prodIds);
		} catch (Exception e) {
			System.err.println("Lỗi xóa sản phẩm đã mua khỏi giỏ hàng database: " + e.getMessage());
		}
	}

	public List<CartItemBean> getCartItems() {
		return getCartItems(null);
	}

	public List<CartItemBean> getCartItems(List<Integer> prodIds) {
		List<CartItemBean> items = new ArrayList<>();
		Map<Integer, Integer> cartMap = getCartMapSession();

		for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
			if (prodIds != null && !prodIds.isEmpty() && !prodIds.contains(entry.getKey())) {
				continue;
			}
			Product product = productServices.findById(entry.getKey());
			if (product != null && product.isStatus()) {
				items.add(new CartItemBean(product, entry.getValue()));
			}
		}

		return items;
	}

	public int getTotalAmount() {
		int total = 0;
		for (CartItemBean item : getCartItems()) {
			total += item.getSubTotal();
		}
		return total;
	}

	public int getTotalQuantity() {
		int count = 0;
		for (Integer qty : getCartMapSession().values()) {
			count += qty;
		}
		return count;
	}

	public Map<Integer, Integer> getCartMapSession() {
		Map<Integer, Integer> cartMap = new HashMap<>();
		String cartJson = (String) session.getAttribute("cart");
		if (cartJson != null && !cartJson.isEmpty()) {
			try {
				cartMap = objectMapper.readValue(cartJson, new TypeReference<Map<Integer, Integer>>() {
				});
			} catch (Exception e) {
				System.err.println("Lỗi parse JSON giỏ hàng: " + e.getMessage());
			}
		}
		return cartMap;
	}

	private void saveCartToSession(Map<Integer, Integer> cartMap) {
		try {
			String newCartJson = objectMapper.writeValueAsString(cartMap);
			session.setAttribute("cart", newCartJson);
			session.setAttribute("cartCount", getTotalQuantity());
			syncCurrentSessionToDatabase(cartMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncCurrentSessionToDatabase(Map<Integer, Integer> cartMap) {
		try {
			Object uidAttr = session.getAttribute("userId");
			if (uidAttr == null) {
				Object userAttr = session.getAttribute("user");
				if (userAttr instanceof User u) {
					uidAttr = u.getId();
				}
			}
			if (uidAttr == null && request != null) {
				String cookieUid = com.fpoly.java5demo.utils.Utils.getCookieValue(com.fpoly.java5demo.utils.Utils.COOKIE_KEY_USER_ID, request);
				if (cookieUid != null && !cookieUid.trim().isEmpty()) {
					uidAttr = cookieUid.trim();
				}
			}
			if (uidAttr != null) {
				int userId = Integer.parseInt(String.valueOf(uidAttr));
				User user = userJPA.findById(userId).orElse(null);
				if (user != null) {
					cartDetailJPA.deleteByUserId(userId);
					if (cartMap != null && !cartMap.isEmpty()) {
						List<CartDetail> list = new ArrayList<>();
						for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
							Product p = productServices.findById(entry.getKey());
							if (p != null) {
								CartDetail cd = new CartDetail();
								cd.setUser(user);
								cd.setProduct(p);
								cd.setQuantity(entry.getValue());
								list.add(cd);
							}
						}
						if (!list.isEmpty()) {
							cartDetailJPA.saveAll(list);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Lỗi đồng bộ giỏ hàng vào SQL Server: " + e.getMessage());
		}
	}
}
