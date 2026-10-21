package com.fpoly.java5demo.services;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fpoly.java5demo.beans.CheckoutBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.OrderDetailJPA;
import com.fpoly.java5demo.jpas.OrderJPA;
import com.fpoly.java5demo.jpas.ProductJPA;
import com.fpoly.java5demo.jpas.UserJPA;

@Service
public class OrderServices {

	@Autowired
	private OrderJPA orderJPA;

	@Autowired
	private OrderDetailJPA orderDetailJPA;

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private ProductJPA productJPA;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Transactional
	public Order createOrder(User user, CheckoutBean checkoutBean) {
		return createOrder(user, checkoutBean, null);
	}

	@Transactional
	public Order createOrder(User user, CheckoutBean checkoutBean, List<Integer> selectedProdIds) {
		if (user == null || user.getRole() == 1) {
			return null; // Tài khoản Admin không được phép đặt mua hàng
		}

		Map<Integer, Integer> allCartItems = cartDetailServices.getCartMapSession();
		if (allCartItems.isEmpty()) {
			return null;
		}

		// Lọc ra các sản phẩm được tích chọn mua (nếu có chỉ định)
		Map<Integer, Integer> cartItems = new HashMap<>();
		if (selectedProdIds != null && !selectedProdIds.isEmpty()) {
			for (Integer pid : selectedProdIds) {
				if (allCartItems.containsKey(pid)) {
					cartItems.put(pid, allCartItems.get(pid));
				}
			}
		} else {
			cartItems.putAll(allCartItems);
		}

		if (cartItems.isEmpty()) {
			return null;
		}

		List<Integer> ids = new ArrayList<>(cartItems.keySet());
		List<Product> products = productJPA.findAllById(ids);

		int totalPrice = 0;
		for (Product item : products) {
			int buyQty = cartItems.getOrDefault(item.getId(), 0);
			if (item.getQuantity() < buyQty || buyQty <= 0) {
				return null; // Không đủ số lượng tồn kho
			}
			totalPrice += buyQty * item.getSalePrice();
		}

		// Tạo đơn hàng
		Order order = new Order();
		String fullAddress = checkoutBean.getAddress() + " (Người nhận: " + checkoutBean.getFullName() + " - SĐT: "
				+ checkoutBean.getPhone() + ")";
		if (checkoutBean.getNote() != null && !checkoutBean.getNote().trim().isEmpty()) {
			fullAddress += " [Ghi chú: " + checkoutBean.getNote().trim() + "]";
		}
		if (fullAddress.length() > 200) {
			fullAddress = fullAddress.substring(0, 200);
		}
		order.setAddress(fullAddress);

		User managedUser = userJPA.findById(user.getId()).orElse(user);
		order.setUser(managedUser);

		Calendar calendar = Calendar.getInstance();
		String dateStr = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		order.setDate(Date.valueOf(dateStr));
		order.setStatus(1); // 1: Chờ xác nhận (chuẩn quy ước của thầy)
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		order.setTotalPrice(totalPrice);

		Order savedOrder = orderJPA.save(order);

		// Lưu chi tiết đơn hàng và trừ kho
		List<OrderDetail> details = new ArrayList<>();
		for (Product item : products) {
			int buyQty = cartItems.get(item.getId());
			OrderDetail detail = new OrderDetail();
			detail.setOrder(savedOrder);
			detail.setProduct(item);
			detail.setPrice(item.getSalePrice());
			detail.setQuantity(buyQty);
			orderDetailJPA.save(detail);
			details.add(detail);

			// Trừ số lượng tồn kho
			item.setQuantity(item.getQuantity() - buyQty);
			productJPA.save(item);
		}
		savedOrder.setOrderDetails(details);

		// Chỉ xóa các sản phẩm đã mua khỏi giỏ hàng; các sản phẩm chưa chọn vẫn giữ
		// nguyên
		cartDetailServices.removeItemsFromCart(user.getId(), ids);

		return savedOrder;
	}

	public List<Order> getOrdersByUser(int userId) {
		return orderJPA.findByUserIdOrderByDateDesc(userId);
	}

	public List<Order> getAllOrders() {
		return orderJPA.findAllByOrderByDateDesc();
	}

	public Order getOrderById(int orderId) {
		return orderJPA.findById(orderId).orElse(null);
	}

	public List<OrderDetail> getOrderDetails(int orderId) {
		return orderDetailJPA.findByOrderId(orderId);
	}

	public long getCompletedRevenue() {
		Long revenue = orderJPA.calculateCompletedRevenue();
		return revenue != null ? revenue : 0L;
	}

	public long getOrderCount() {
		return orderJPA.count();
	}

	public long getPendingOrderCount() {
		// Trạng thái 1: Chờ xác nhận
		return orderJPA.countByStatus(1);
	}

	public List<Order> getRecentOrders(int limit) {
		List<Order> all = orderJPA.findAllByOrderByDateDesc();
		if (all.size() <= limit) {
			return all;
		}
		return all.subList(0, limit);
	}

	/**
	 * Cập nhật trạng thái đơn hàng (Admin):
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated at
	 * - Trạng thái đơn hàng chỉ được cập nhật theo 1 chiều duy nhất:
	 * + 1 -> 2 -> 3 -> 4 (Đã giao hàng)
	 * + 1 -> 6 (Huỷ đơn)
	 * + 2 -> 6 (Huỷ đơn)
	 */
	@Transactional
	public boolean updateStatus(int orderId, int newStatus) {
		Order order = getOrderById(orderId);
		if (order == null) {
			return false;
		}

		int currentStatus = order.getStatus();

		// Kiểm tra quy tắc chuyển 1 chiều duy nhất
		boolean isValid = false;
		if (currentStatus == 1 && (newStatus == 2 || newStatus == 6)) {
			isValid = true;
		} else if (currentStatus == 2 && (newStatus == 3 || newStatus == 6)) {
			isValid = true;
		} else if (currentStatus == 3 && newStatus == 4) {
			isValid = true;
		}

		if (!isValid) {
			return false;
		}

		// Nếu chuyển sang hủy (6), hoàn lại tồn kho
		if (newStatus == 6) {
			restoreStock(orderId);
		}

		order.setStatus(newStatus);
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		orderJPA.save(order);
		return true;
	}

	/**
	 * Chức năng huỷ đơn hàng (Admin):
	 * - Kiểm tra trạng thái đơn "Chờ xác nhận" == 1 hoặc "Đã xác nhận" == 2 thì mới
	 * được huỷ (sang 6)
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated at
	 */
	@Transactional
	public boolean adminCancelOrder(int orderId) {
		Order order = getOrderById(orderId);
		if (order == null) {
			return false;
		}

		int currentStatus = order.getStatus();
		if (currentStatus == 1 || currentStatus == 2) {
			order.setStatus(6); // 6: Huỷ đơn
			order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			orderJPA.save(order);
			restoreStock(orderId);
			return true;
		}
		return false;
	}

	/**
	 * Chức năng huỷ đơn hàng (User):
	 * - Kiểm tra đơn hàng có phải của user sở hữu không?
	 * - Kiểm tra trạng thái đơn "Chờ xác nhận" == 1 => Thì mới được huỷ (sang 6)
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated at
	 */
	@Transactional
	public boolean cancelOrder(int orderId, int userId) {
		Order order = getOrderById(orderId);
		if (order == null || order.getUser() == null || order.getUser().getId() != userId) {
			return false;
		}

		// Chỉ cho hủy khi đơn hàng ở trạng thái 1 (Chờ xác nhận)
		if (order.getStatus() == 1) {
			order.setStatus(6); // 6: Huỷ đơn
			order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			orderJPA.save(order);
			restoreStock(orderId);
			return true;
		}
		return false;
	}

	/**
	 * Chức năng xác nhận đơn hàng (User):
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated at
	 * - Chỉ được xác nhận khi trạng thái đơn trước đó là 4 ("Đã giao hàng") =>
	 * chuyển sang 5 ("Đã nhận hàng / Hoàn thành")
	 */
	@Transactional
	public boolean confirmReceived(int orderId, int userId) {
		Order order = getOrderById(orderId);
		if (order == null || order.getUser() == null || order.getUser().getId() != userId) {
			return false;
		}

		// Chỉ được xác nhận khi trạng thái đơn trước đó là 4 ("Đã giao hàng")
		if (order.getStatus() == 4) {
			order.setStatus(5); // 5: Đã nhận hàng / Hoàn thành
			order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
			orderJPA.save(order);
			return true;
		}
		return false;
	}

	private void restoreStock(int orderId) {
		List<OrderDetail> details = orderDetailJPA.findByOrderId(orderId);
		for (OrderDetail detail : details) {
			Product p = detail.getProduct();
			if (p != null) {
				p.setQuantity(p.getQuantity() + detail.getQuantity());
				productJPA.save(p);
			}
		}
	}
}
