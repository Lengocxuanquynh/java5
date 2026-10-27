package com.fpoly.java5demo.services;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fpoly.java5demo.beans.CartItemBean;
import com.fpoly.java5demo.beans.CheckoutBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.OrderDetailJPA;
import com.fpoly.java5demo.jpas.OrderJPA;
import com.fpoly.java5demo.jpas.ProductJPA;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

// [Vinh] Service xử lý luồng bán hàng
// Dùng Cookie lấy userId, Optional kiểm tra, throw RuntimeException khi lỗi
@Service
public class OrderServices {

	@Autowired
	private HttpServletRequest request;

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

	/**
	 * [Vinh] Lấy danh sách sản phẩm hiển thị trên trang Checkout:
	 * Nếu user tích chọn các sản phẩm cụ thể thì lấy theo items, ngược lại lấy toàn bộ giỏ
	 */
	public List<CartItemBean> getCheckoutCartItems(List<Integer> itemIds) {
		if (itemIds != null && !itemIds.isEmpty()) {
			return cartDetailServices.getCartItems(itemIds);
		}
		return cartDetailServices.getCartItems();
	}

	/**
	 * [Vinh] Tính tổng tiền của danh sách sản phẩm checkout
	 */
	public int calculateTotalAmount(List<CartItemBean> cartItems) {
		int total = 0;
		if (cartItems != null) {
			for (CartItemBean item : cartItems) {
				total += item.getSubTotal();
			}
		}
		return total;
	}

	/**
	 * [Vinh] Tính tổng số lượng sản phẩm trong giỏ checkout
	 */
	public int calculateTotalQuantity(List<CartItemBean> cartItems) {
		int count = 0;
		if (cartItems != null) {
			for (CartItemBean item : cartItems) {
				count += item.getQuantity();
			}
		}
		return count;
	}

	/**
	 * [Vinh] Khởi tạo form CheckoutBean điền sẵn thông tin user và tổng tiền dự kiến
	 */
	public CheckoutBean initCheckoutBean(User user, int totalAmount) {
		CheckoutBean bean = new CheckoutBean();
		if (user != null) {
			bean.setFullName(user.getName());
			bean.setPhone(user.getPhone());
			bean.setAddress(user.getAddress());
		}
		bean.setExpectedTotal(totalAmount);
		return bean;
	}

	/**
	 * [Vinh] Tạo đơn hàng - đặt toàn bộ hoặc chỉ các sản phẩm được chọn từ giỏ
	 * hàng.
	 * Lưu riêng 4 cột: shippingName, shippingPhone, shippingAddress, note

	 * Trừ tồn kho khi đặt hàng thành công. Xóa sản phẩm đã mua khỏi giỏ.
	 * 
	 * @return id đơn hàng vừa tạo
	 */
	public Order createOrder(User user, CheckoutBean bean) {
		if (user == null || user.getRole() == 1) {
			return null;
		}
		Map<Integer, Integer> allCartItems = cartDetailServices.getCartMap(user.getId());
		if (allCartItems.isEmpty()) {
			return null;
		}
		List<Integer> ids = new ArrayList<>(allCartItems.keySet());
		List<Product> products = productJPA.findAllById(ids);
		int totalPrice = 0;
		for (Product item : products) {
			int buyQty = allCartItems.getOrDefault(item.getId(), 0);
			if (item.getQuantity() < buyQty || buyQty <= 0) {
				return null;
			}
			totalPrice += buyQty * item.getSalePrice();
		}
		Order order = new Order();
		order.setShippingName(bean.getFullName());
		order.setShippingPhone(bean.getPhone());
		order.setShippingAddress(bean.getAddress());
		order.setNote(bean.getNote());
		order.setTotalPrice(totalPrice);
		order.setUser(user);
		order.setStatus(1);
		Calendar calendar = Calendar.getInstance();
		String dateStr = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		order.setDate(Date.valueOf(dateStr));
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

		Order savedOrder = null;
		List<OrderDetail> detailList = new ArrayList<>();
		Map<Product, Integer> deductedProducts = new HashMap<>();

		try {
			// 1. Lưu Order
			savedOrder = orderJPA.save(order);

			// 2. Lưu từng chi tiết và trừ kho
			for (Product item : products) {
				int buyQty = allCartItems.get(item.getId());
				OrderDetail detail = new OrderDetail();
				detail.setOrder(savedOrder);
				detail.setProduct(item);
				detail.setPrice(item.getSalePrice());
				detail.setQuantity(buyQty);
				OrderDetail savedDetail = orderDetailJPA.save(detail);
				detailList.add(savedDetail);

				item.setQuantity(item.getQuantity() - buyQty);
				if (item.getQuantity() <= 0) {
					item.setQuantity(0);
					item.setStatus(false); // Hết hàng trong kho -> tự động ẩn sản phẩm
				}
				productJPA.save(item);
				deductedProducts.put(item, buyQty);
			}
			savedOrder.setOrderDetails(detailList);

			// 3. Xóa khỏi giỏ hàng
			cartDetailServices.removeItemsFromCart(user.getId(), ids);
			return savedOrder;
		} catch (Exception e) {
			// ROLLBACK THỦ CÔNG:
			// a. Hoàn lại kho cho các sản phẩm đã trừ
			for (Map.Entry<Product, Integer> entry : deductedProducts.entrySet()) {
				try {
					Product prod = entry.getKey();
					prod.setQuantity(prod.getQuantity() + entry.getValue());
					productJPA.save(prod);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			// b. Xóa chi tiết đã lỡ lưu
			for (OrderDetail d : detailList) {
				try {
					orderDetailJPA.delete(d);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			// c. Xóa đơn hàng đã lỡ tạo
			if (savedOrder != null && savedOrder.getId() > 0) {
				try {
					orderJPA.delete(savedOrder);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return null;
		}
	}

	public int createOrder(CheckoutBean bean) {
		return createOrder(bean, null);
	}

	public int createOrder(CheckoutBean bean, List<Integer> selectedProdIds) {
		// Lấy user id từ cookie
		String userIdStr = Utils.getCookieValue("user_id", request);
		if (userIdStr == null || userIdStr.trim().isEmpty()) {
			throw new RuntimeException("Vui lòng đăng nhập để thực hiện đặt hàng!");
		}

		int userId;
		try {
			userId = Integer.parseInt(userIdStr.trim());
		} catch (NumberFormatException e) {
			throw new RuntimeException("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại!");
		}

		Optional<User> userOptional = userJPA.findById(userId);

		if (!userOptional.isPresent()) {
			throw new RuntimeException("Không tìm thấy tài khoản người dùng!");
		}

		User user = userOptional.get();

		// Kiểm tra tài khoản có bị khóa không
		if (!user.isStatus()) {
			throw new RuntimeException("Tài khoản đã bị khóa!");
		}

		// Tài khoản Admin không được phép đặt hàng
		if (user.getRole() == 1) {
			throw new RuntimeException("Tài khoản Admin không được phép đặt hàng!");
		}

		// Lấy giỏ hàng từ database theo user_id
		Map<Integer, Integer> allCartItems = cartDetailServices.getCartMap(user.getId());
		if (allCartItems.isEmpty()) {
			throw new RuntimeException("Giỏ hàng trống!");
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
			throw new RuntimeException("Không có sản phẩm nào được chọn!");
		}

		// Lấy danh sách sản phẩm từ DB
		List<Integer> ids = new ArrayList<>(cartItems.keySet());
		List<Product> products = productJPA.findAllById(ids);

		// Kiểm tra sản phẩm bị xóa khỏi DB (Admin xóa trong lúc user đang checkout)
		if (products.size() != ids.size()) {
			throw new RuntimeException(
					"Một số sản phẩm trong giỏ hàng không còn tồn tại. Vui lòng kiểm tra lại giỏ hàng!");
		}

		// Tính tổng tiền và kiểm tra tồn kho / trạng thái sản phẩm
		int totalPrice = 0;
		for (Product item : products) {
			// Kiểm tra sản phẩm bị ẩn (Admin ẩn trong lúc user đang checkout)
			if (!item.isStatus()) {
				throw new RuntimeException(
						"Sản phẩm '" + item.getName() + "' hiện không còn được bán. Vui lòng kiểm tra lại giỏ hàng!");
			}
			int buyQty = cartItems.getOrDefault(item.getId(), 0);
			if (item.getQuantity() <= 0) {
				// Sản phẩm đã hết hàng trong kho -> tự động dọn khỏi giỏ hàng
				cartDetailServices.removeCart(userId, item.getId());
				throw new RuntimeException(
						"Sản phẩm '" + item.getName() + "' hiện đã hết hàng trong kho. Hệ thống đã xóa sản phẩm khỏi giỏ hàng của bạn!");
			}
			if (item.getQuantity() < buyQty) {
				// Tồn kho không đủ số lượng muốn mua -> tự động hạ giỏ hàng xuống bằng tồn kho còn lại
				cartDetailServices.updateQuantity(userId, item.getId(), item.getQuantity());
				throw new RuntimeException(
						"Sản phẩm '" + item.getName() + "' trong kho chỉ còn " + item.getQuantity()
								+ " cái (bạn đang chọn " + buyQty
								+ " cái). Hệ thống đã tự động điều chỉnh số lượng trong giỏ hàng về "
								+ item.getQuantity() + ". Vui lòng kiểm tra lại đơn hàng!");
			}
			if (buyQty <= 0) {
				throw new RuntimeException(
						"Số lượng mua sản phẩm '" + item.getName() + "' không hợp lệ!");
			}
			totalPrice += buyQty * item.getSalePrice();
		}

		// Kiểm tra lệch giá: Nếu Admin đổi giá sản phẩm trước khi user bấm đặt hàng
		if (bean.getExpectedTotal() != null && bean.getExpectedTotal() > 0
				&& bean.getExpectedTotal().intValue() != totalPrice) {
			throw new RuntimeException(
					"Giá sản phẩm đã được thay đổi trong lúc bạn đặt hàng (Tổng tiền cập nhật từ "
							+ String.format("%,d", bean.getExpectedTotal()) + " ₫ thành "
							+ String.format("%,d", totalPrice) + " ₫). Vui lòng kiểm tra lại đơn hàng!");
		}

		// Tạo đơn hàng - lưu riêng 4 cột thông tin giao hàng
		Order order = new Order();
		order.setShippingName(bean.getFullName());
		order.setShippingPhone(bean.getPhone());
		order.setShippingAddress(bean.getAddress());
		order.setNote(bean.getNote());
		order.setTotalPrice(totalPrice);
		order.setUser(user);
		order.setStatus(1); // 1: Chờ xác nhận

		Calendar calendar = Calendar.getInstance();
		String dateStr = String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		order.setDate(Date.valueOf(dateStr));
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

		Order savedOrder = null;
		List<OrderDetail> savedDetails = new ArrayList<>();
		Map<Product, Integer> deductedProducts = new HashMap<>();

		try {
			// 1. Lưu Order
			savedOrder = orderJPA.save(order);

			// 2. Lưu từng chi tiết và trừ kho
			for (Product item : products) {
				int buyQty = cartItems.get(item.getId());

				OrderDetail detail = new OrderDetail();
				detail.setOrder(savedOrder);
				detail.setProduct(item);
				detail.setPrice(item.getSalePrice());
				detail.setQuantity(buyQty);
				OrderDetail savedDetail = orderDetailJPA.save(detail);
				savedDetails.add(savedDetail);

				// Trừ số lượng tồn kho
				item.setQuantity(item.getQuantity() - buyQty);
				if (item.getQuantity() <= 0) {
					item.setQuantity(0);
					item.setStatus(false); // Hết hàng trong kho -> tự động ẩn sản phẩm
				}
				productJPA.save(item);
				deductedProducts.put(item, buyQty); // Ghi nhận lại sản phẩm và số lượng đã trừ kho
			}

			// 3. Xóa các sản phẩm đã mua khỏi giỏ hàng
			cartDetailServices.removeItemsFromCart(userId, ids);

			return savedOrder.getId();
		} catch (Exception e) {
			// NẾU GẶP LỖI Ở BẤT KỲ ĐÂU: TỰ TAY ROLLBACK (DỌN DẸP RÁC)
			// a. Hoàn lại kho cho các sản phẩm đã trừ
			for (Map.Entry<Product, Integer> entry : deductedProducts.entrySet()) {
				try {
					Product prod = entry.getKey();
					prod.setQuantity(prod.getQuantity() + entry.getValue());
					productJPA.save(prod);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// b. Xóa chi tiết đơn hàng đã lỡ lưu
			for (OrderDetail d : savedDetails) {
				try {
					orderDetailJPA.delete(d);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// c. Xóa đơn hàng đã lỡ tạo
			if (savedOrder != null && savedOrder.getId() > 0) {
				try {
					orderJPA.delete(savedOrder);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			// Ném lỗi tiếp để Controller bắt và hiển thị thông báo
			throw new RuntimeException("Đặt hàng không thành công: " + e.getMessage(), e);
		}
	}

	/**
	 * [Vinh] Lấy đơn hàng theo ID - kiểm tra quyền sở hữu theo userId từ Cookie
	 */
	public Order getOrderById(int orderId) {
		Optional<Order> orderOptional = orderJPA.findById(orderId);
		if (!orderOptional.isPresent()) {
			return null;
		}
		return orderOptional.get();
	}

	public List<Order> getOrdersByUser(int userId) {
		return orderJPA.findByUserIdOrderByDateDesc(userId);
	}

	public List<Order> getAllOrders() {
		return orderJPA.findAllByOrderByDateDesc();
	}

	/**
	 * [Vinh] Lấy các đơn hàng ĐANG XỬ LÝ (status 1, 2, 3, 4) để admin thao tác thuận tiện
	 */
	public List<Order> getProcessingOrders() {
		return getAllOrders().stream()
				.filter(o -> o.getStatus() >= 1 && o.getStatus() <= 4)
				.toList();
	}

	/**
	 * [Vinh] Lấy các đơn hàng ĐÃ HOÀN TẤT / KẾT THÚC (status 5: Đã nhận hàng, status 6: Đã hủy)
	 */
	public List<Order> getCompletedOrders() {
		return getAllOrders().stream()
				.filter(o -> o.getStatus() == 5 || o.getStatus() == 6)
				.toList();
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
	 * [Vinh] Cập nhật trạng thái đơn hàng (Admin):
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated_at
	 * - Trạng thái đơn hàng chỉ được cập nhật theo 1 chiều duy nhất:
	 *   + 1 -> 2 -> 3 -> 4 (Đã giao hàng)
	 *   + 1 -> 6 (Huỷ đơn)
	 *   + 2 -> 6 (Huỷ đơn)
	 */
	public boolean updateStatus(int orderId, int newStatus, String cancelReason) {
		Optional<Order> orderOptional = orderJPA.findById(orderId);
		if (!orderOptional.isPresent()) {
			return false;
		}

		Order order = orderOptional.get();
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

		// Nếu chuyển sang hủy (6), hoàn lại tồn kho và lưu lý do (có try-catch rollback)
		if (newStatus == 6) {
			String oldReason = order.getCancelReason();
			try {
				if (cancelReason != null && !cancelReason.trim().isEmpty()) {
					order.setCancelReason("Admin hủy: " + cancelReason.trim());
				} else {
					order.setCancelReason("Admin hủy đơn hàng");
				}
				order.setStatus(newStatus);
				order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
				orderJPA.save(order);
				restoreStock(orderId);
				return true;
			} catch (Exception e) {
				// Rollback lại trạng thái cũ nếu hoàn kho thất bại
				order.setStatus(currentStatus);
				order.setCancelReason(oldReason);
				orderJPA.save(order);
				throw new RuntimeException("Cập nhật hủy đơn thất bại: " + e.getMessage(), e);
			}
		}

		order.setStatus(newStatus);
		order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		orderJPA.save(order);
		return true;
	}

	public boolean updateStatus(int orderId, int newStatus) {
		return updateStatus(orderId, newStatus, null);
	}

	/**
	 * [Vinh] Chức năng huỷ đơn hàng (Admin):
	 * - Kiểm tra trạng thái đơn "Chờ xác nhận" == 1 hoặc "Đã xác nhận" == 2
	 *   thì mới được huỷ (sang 6)
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated_at
	 */
	public boolean adminCancelOrder(int orderId, String cancelReason) {
		Optional<Order> orderOptional = orderJPA.findById(orderId);
		if (!orderOptional.isPresent()) {
			return false;
		}

		Order order = orderOptional.get();
		int currentStatus = order.getStatus();
		String oldReason = order.getCancelReason();
		if (currentStatus == 1 || currentStatus == 2) {
			try {
				order.setStatus(6); // 6: Huỷ đơn
				if (cancelReason != null && !cancelReason.trim().isEmpty()) {
					order.setCancelReason("Admin hủy: " + cancelReason.trim());
				} else {
					order.setCancelReason("Admin hủy đơn hàng");
				}
				order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
				orderJPA.save(order);
				restoreStock(orderId);
				return true;
			} catch (Exception e) {
				// Rollback lại trạng thái cũ nếu hoàn kho lỗi
				order.setStatus(currentStatus);
				order.setCancelReason(oldReason);
				orderJPA.save(order);
				throw new RuntimeException("Hủy đơn hàng thất bại: " + e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean adminCancelOrder(int orderId) {
		return adminCancelOrder(orderId, null);
	}

	/**
	 * [Vinh] Chức năng huỷ đơn hàng (User):
	 * - Kiểm tra đơn hàng có phải của user sở hữu không?
	 * - Kiểm tra trạng thái đơn "Chờ xác nhận" == 1 => Thì mới được huỷ (sang 6)
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated_at
	 */
	public boolean cancelOrder(int orderId, int userId, String cancelReason) {
		Optional<Order> orderOptional = orderJPA.findById(orderId);
		if (!orderOptional.isPresent()) {
			return false;
		}

		Order order = orderOptional.get();
		if (order.getUser() == null || order.getUser().getId() != userId) {
			return false;
		}

		// Chỉ cho hủy khi đơn hàng ở trạng thái 1 (Chờ xác nhận)
		if (order.getStatus() == 1) {
			int currentStatus = order.getStatus();
			String oldReason = order.getCancelReason();
			try {
				order.setStatus(6); // 6: Huỷ đơn
				if (cancelReason != null && !cancelReason.trim().isEmpty()) {
					order.setCancelReason("Khách hàng hủy: " + cancelReason.trim());
				} else {
					order.setCancelReason("Khách hàng yêu cầu hủy đơn");
				}
				order.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
				orderJPA.save(order);
				restoreStock(orderId);
				return true;
			} catch (Exception e) {
				// Rollback lại trạng thái cũ nếu hoàn kho lỗi
				order.setStatus(currentStatus);
				order.setCancelReason(oldReason);
				orderJPA.save(order);
				throw new RuntimeException("Hủy đơn hàng thất bại: " + e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean cancelOrder(int orderId, int userId) {
		return cancelOrder(orderId, userId, null);
	}

	/**
	 * [Vinh] Chức năng xác nhận nhận hàng (User):
	 * - Trạng thái đơn luôn kiểm tra ở DB
	 * - Khi thay đổi nhớ cập nhật giá trị của updated_at
	 * - Chỉ được xác nhận khi trạng thái đơn trước đó là 4 ("Đã giao hàng")
	 *   => chuyển sang 5 ("Đã nhận hàng / Hoàn thành")
	 */
	public boolean confirmReceived(int orderId, int userId) {
		Optional<Order> orderOptional = orderJPA.findById(orderId);
		if (!orderOptional.isPresent()) {
			return false;
		}

		Order order = orderOptional.get();
		if (order.getUser() == null || order.getUser().getId() != userId) {
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

	/**
	 * [Vinh] Hoàn lại tồn kho khi hủy đơn
	 */
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
