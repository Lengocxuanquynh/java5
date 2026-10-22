package com.fpoly.java5demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fpoly.java5demo.beans.CartItemBean;
import com.fpoly.java5demo.entities.CartDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.CartDetailJPA;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

// [CartDetailServices] Quản lý giỏ hàng trực tiếp từ Database theo user_id (Cookie)
// Loại bỏ hoàn toàn HttpSession - Code mộc mạc chuẩn style thầy (Java5-SD21201)
@Service
public class CartDetailServices {

	@Autowired
	private CartDetailJPA cartDetailJPA;

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private ProductServices productServices;

	@Autowired(required = false)
	private HttpServletRequest request;

	public CartDetailServices() {
	}

	private int getLoggedInUserId() {
		try {
			if (request != null) {
				String uidStr = Utils.getCookieValue(Utils.COOKIE_KEY_USER_ID, request);
				if (uidStr != null && !uidStr.trim().isEmpty()) {
					return Integer.parseInt(uidStr.trim());
				}
			}
		} catch (Exception ignored) {
		}
		return 0;
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

	public boolean addToCart(int userId, int prodId, int count) {
		if (userId <= 0 || count <= 0) {
			return false;
		}
		Product product = productServices.findById(prodId);
		if (product == null || !product.isStatus()) {
			return false;
		}

		Optional<CartDetail> opt = cartDetailJPA.findByUserIdAndProductId(userId, prodId);
		if (opt.isPresent()) {
			CartDetail cd = opt.get();
			int newQty = cd.getQuantity() + count;
			if (newQty > product.getQuantity()) {
				newQty = product.getQuantity();
			}
			if (newQty <= 0) {
				cartDetailJPA.delete(cd);
			} else {
				cd.setQuantity(newQty);
				cartDetailJPA.save(cd);
			}
		} else {
			int qty = count;
			if (qty > product.getQuantity()) {
				qty = product.getQuantity();
			}
			if (qty > 0) {
				Optional<User> userOpt = userJPA.findById(userId);
				if (userOpt.isPresent()) {
					CartDetail cd = new CartDetail();
					cd.setUser(userOpt.get());
					cd.setProduct(product);
					cd.setQuantity(qty);
					cartDetailJPA.save(cd);
				}
			}
		}
		return true;
	}

	public boolean addToCart(int prodId, int count) {
		int userId = getLoggedInUserId();
		if (userId <= 0) {
			return false;
		}
		return addToCart(userId, prodId, count);
	}

	public boolean updateQuantity(int userId, int prodId, int quantity) {
		if (userId <= 0) {
			return false;
		}
		Product product = productServices.findById(prodId);
		if (product == null) {
			return false;
		}

		Optional<CartDetail> opt = cartDetailJPA.findByUserIdAndProductId(userId, prodId);
		if (opt.isPresent()) {
			CartDetail cd = opt.get();
			if (quantity <= 0) {
				cartDetailJPA.delete(cd);
			} else {
				if (quantity > product.getQuantity()) {
					quantity = product.getQuantity();
				}
				cd.setQuantity(quantity);
				cartDetailJPA.save(cd);
			}
			return true;
		}
		return false;
	}

	public boolean updateQuantity(int prodId, int quantity) {
		int userId = getLoggedInUserId();
		if (userId <= 0) {
			return false;
		}
		return updateQuantity(userId, prodId, quantity);
	}

	public boolean removeCart(int userId, int prodId) {
		if (userId <= 0) {
			return false;
		}
		cartDetailJPA.deleteByUserIdAndProductId(userId, prodId);
		return true;
	}

	public boolean removeCart(int prodId) {
		int userId = getLoggedInUserId();
		if (userId <= 0) {
			return false;
		}
		return removeCart(userId, prodId);
	}

	public void clearCart(int userId) {
		if (userId > 0) {
			cartDetailJPA.deleteByUserId(userId);
		}
	}

	public void clearCart() {
		int userId = getLoggedInUserId();
		if (userId > 0) {
			clearCart(userId);
		}
	}

	public void removeItemsFromCart(int userId, List<Integer> prodIds) {
		if (userId <= 0) {
			return;
		}
		if (prodIds == null || prodIds.isEmpty()) {
			clearCart(userId);
			return;
		}
		cartDetailJPA.deleteByUserIdAndProductIds(userId, prodIds);
	}

	public List<CartItemBean> getCartItems(int userId, List<Integer> prodIds) {
		List<CartItemBean> items = new ArrayList<>();
		if (userId <= 0) {
			return items;
		}
		List<CartDetail> details = cartDetailJPA.findByUserId(userId);
		for (CartDetail cd : details) {
			if (prodIds != null && !prodIds.isEmpty() && !prodIds.contains(cd.getProduct().getId())) {
				continue;
			}
			Product product = cd.getProduct();
			if (product != null && product.isStatus()) {
				items.add(new CartItemBean(product, cd.getQuantity()));
			}
		}
		return items;
	}

	public List<CartItemBean> getCartItems() {
		int userId = getLoggedInUserId();
		return getCartItems(userId, null);
	}

	public List<CartItemBean> getCartItems(List<Integer> prodIds) {
		int userId = getLoggedInUserId();
		return getCartItems(userId, prodIds);
	}

	public List<CartItemBean> getCartItems(int userId) {
		return getCartItems(userId, null);
	}

	public int getTotalAmount(int userId) {
		int total = 0;
		for (CartItemBean item : getCartItems(userId, null)) {
			total += item.getSubTotal();
		}
		return total;
	}

	public int getTotalAmount() {
		int userId = getLoggedInUserId();
		return getTotalAmount(userId);
	}

	public int getTotalQuantity(int userId) {
		if (userId <= 0) {
			return 0;
		}
		int count = 0;
		List<CartDetail> details = cartDetailJPA.findByUserId(userId);
		for (CartDetail cd : details) {
			count += cd.getQuantity();
		}
		return count;
	}

	public int getTotalQuantity() {
		int userId = getLoggedInUserId();
		return getTotalQuantity(userId);
	}

	public Map<Integer, Integer> getCartMap(int userId) {
		Map<Integer, Integer> cartMap = new HashMap<>();
		if (userId <= 0) {
			return cartMap;
		}
		List<CartDetail> details = cartDetailJPA.findByUserId(userId);
		for (CartDetail cd : details) {
			if (cd.getProduct() != null && cd.getProduct().isStatus()) {
				cartMap.put(cd.getProduct().getId(), cd.getQuantity());
			}
		}
		return cartMap;
	}

	public Map<Integer, Integer> getCartMap() {
		int userId = getLoggedInUserId();
		return getCartMap(userId);
	}

	// Tương thích ngược cho các class gọi method cũ
	public Map<Integer, Integer> getCartMapSession() {
		return getCartMap();
	}

	public boolean syncSessionCartToDatabase(List<CartDetail> cartDetails) {
		return true;
	}

	public void loadCartFromDatabaseToSession(int userId) {
		// No-op: Cart được load trực tiếp từ DB
	}
}
