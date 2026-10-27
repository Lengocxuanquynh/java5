package com.fpoly.java5demo.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.fpoly.java5demo.beans.CancelOrderBean;
import com.fpoly.java5demo.beans.CartItemBean;
import com.fpoly.java5demo.beans.CheckoutBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.OrderServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

// [Vinh] Controller xử lý luồng bán hàng
// Dùng Cookie + Interceptor lấy user, try-catch bắt lỗi, redirect khi thất bại
@Controller
public class OrderController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private OrderServices orderServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	private User getCurrentUser() {
		return userServices.getLoggedInUser(request);
	}

	/**
	 * [Vinh] Trang checkout - điền thông tin đặt hàng
	 * Tự động kéo SĐT và Địa chỉ từ tài khoản user lên form (nhưng vẫn cho sửa)
	 */
	@GetMapping({ "/user/checkout", "/checkout" })
	public String checkoutPage(@RequestParam(name = "items", required = false) List<Integer> items,
			Model model) {
		try {
			User currentUser = getCurrentUser();

			if (currentUser == null) {
				return "redirect:/login?redirect=/user/checkout";
			}

			// Admin không được mua hàng
			if (currentUser.getRole() == 1) {
				return "redirect:/cart?error=admin_cannot_buy";
			}

			// Lấy danh sách sản phẩm trong giỏ qua OrderServices
			List<CartItemBean> cartItems = orderServices.getCheckoutCartItems(items);
			if (cartItems.isEmpty()) {
				return "redirect:/cart";
			}

			int totalAmount = orderServices.calculateTotalAmount(cartItems);
			int totalQuantity = orderServices.calculateTotalQuantity(cartItems);

			// Tự động điền thông tin từ tài khoản user qua OrderServices
			CheckoutBean bean = orderServices.initCheckoutBean(currentUser, totalAmount);

			model.addAttribute("currentUser", currentUser);
			model.addAttribute("checkoutBean", bean);
			model.addAttribute("cartItems", cartItems);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("cartCount", totalQuantity);
			model.addAttribute("selectedItems", items);

			return "checkout";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/login?redirect=/user/checkout";
		}
	}

	/**
	 * [Vinh] Xử lý đặt hàng - POST

	 */
	@PostMapping({ "/user/checkout", "/checkout" })
	public String placeOrder(@Valid @ModelAttribute("checkoutBean") CheckoutBean bean, Errors errors,
			@RequestParam(name = "items", required = false) List<Integer> items,
			Model model) {
		try {
			User currentUser = getCurrentUser();

			if (currentUser == null) {
				return "redirect:/login?redirect=/user/checkout";
			}

			if (currentUser.getRole() == 1) {
				return "redirect:/cart?error=admin_cannot_buy";
			}

			List<CartItemBean> cartItems = orderServices.getCheckoutCartItems(items);
			if (cartItems.isEmpty()) {
				return "redirect:/cart";
			}

			int totalAmount = orderServices.calculateTotalAmount(cartItems);
			int totalQuantity = orderServices.calculateTotalQuantity(cartItems);

			if (errors.hasErrors()) {
				model.addAttribute("currentUser", currentUser);
				model.addAttribute("cartItems", cartItems);
				model.addAttribute("totalAmount", totalAmount);
				model.addAttribute("cartCount", totalQuantity);
				model.addAttribute("selectedItems", items);
				return "checkout";
			}

			// Tạo đơn hàng
			int orderId = orderServices.createOrder(bean, items);

			return "redirect:/user/order-success/" + orderId;
		} catch (Exception e) {
			e.printStackTrace();
			User currentUser = getCurrentUser();
			List<CartItemBean> cartItems = orderServices.getCheckoutCartItems(items);
			int totalAmount = orderServices.calculateTotalAmount(cartItems);
			int totalQuantity = orderServices.calculateTotalQuantity(cartItems);

			model.addAttribute("errorMessage",
					e.getMessage() != null ? e.getMessage() : "Đặt hàng không thành công. Vui lòng thử lại!");
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("cartItems", cartItems);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("cartCount", totalQuantity);
			model.addAttribute("selectedItems", items);
			return "checkout";
		}
	}

	@GetMapping({ "/user/order-success/{id}", "/order/success/{id}" })
	public String orderSuccess(@PathVariable("id") int id, Model model) {
		try {
			User currentUser = getCurrentUser();

			if (currentUser == null) {
				return "redirect:/login";
			}

			Order order = orderServices.getOrderById(id);
			if (order == null || (order.getUser().getId() != currentUser.getId()
					&& currentUser.getRole() != 1)) {
				return "redirect:/";
			}

			List<OrderDetail> details = orderServices.getOrderDetails(id);
			model.addAttribute("order", order);
			model.addAttribute("orderDetails", details);
			model.addAttribute("currentUser", currentUser);
			return "order-success";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/";
		}
	}

	@GetMapping("/user/orders")
	public String myOrders(@RequestParam(name = "msg", required = false) String msg,
			@RequestParam(name = "error", required = false) String error,
			Model model) {
		try {
			User currentUser = getCurrentUser();

			if (currentUser == null) {
				return "redirect:/login?redirect=/user/orders";
			}

			// Admin được chuyển sang trang quản lý đơn hàng toàn sàn
			if (currentUser.getRole() == 1) {
				return "redirect:/admin/orders";
			}

			if ("cancel_success".equals(msg)) {
				model.addAttribute("successMessage", "Đã hủy đơn hàng thành công!");
			} else if ("received_success".equals(msg)) {
				model.addAttribute("successMessage", "Xác nhận đã nhận đơn hàng thành công!");
			}

			if ("missing_reason".equals(error)) {
				model.addAttribute("errorMessage", "Vui lòng nhập lý do hủy đơn hàng!");
			} else if ("cancel_failed".equals(error)) {
				model.addAttribute("errorMessage", "Không thể hủy đơn hàng (chỉ có thể hủy khi đơn ở trạng thái 'Chờ xác nhận')!");
			} else if ("received_failed".equals(error)) {
				model.addAttribute("errorMessage", "Không thể xác nhận (chỉ áp dụng cho đơn ở trạng thái 'Đã giao hàng')!");
			} else if ("cancel_error".equals(error)) {
				model.addAttribute("errorMessage", "Có lỗi xảy ra khi hủy đơn hàng. Vui lòng thử lại!");
			} else if ("received_error".equals(error)) {
				model.addAttribute("errorMessage", "Có lỗi xảy ra khi xác nhận nhận hàng!");
			}

			List<Order> orders = orderServices.getOrdersByUser(currentUser.getId());
			List<Order> processingOrders = new ArrayList<>();
			List<Order> completedOrders = new ArrayList<>();
			for (Order o : orders) {
				if (o.getStatus() >= 1 && o.getStatus() <= 4) {
					processingOrders.add(o);
				} else if (o.getStatus() == 5 || o.getStatus() == 6) {
					completedOrders.add(o);
				}
			}

			model.addAttribute("orders", orders);
			model.addAttribute("processingOrders", processingOrders);
			model.addAttribute("completedOrders", completedOrders);
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			return "user/orders";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/login?redirect=/user/orders";
		}
	}

	@GetMapping("/user/order/{id}")
	public String myOrderDetail(@PathVariable("id") int id, Model model) {
		try {
			User currentUser = getCurrentUser();

			if (currentUser == null) {
				return "redirect:/login?redirect=/user/orders";
			}

			Order order = orderServices.getOrderById(id);
			if (order == null || order.getUser().getId() != currentUser.getId()) {
				return "redirect:/user/orders";
			}

			List<OrderDetail> details = orderServices.getOrderDetails(id);
			model.addAttribute("order", order);
			model.addAttribute("orderDetails", details);
			model.addAttribute("currentUser", currentUser);
			return "user/order-detail";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/orders";
		}
	}

	@PostMapping("/user/order/cancel/{id}")
	public String cancelMyOrder(@PathVariable("id") int id,
			@Valid @ModelAttribute("cancelBean") CancelOrderBean cancelBean, Errors errors) {
		try {
			User currentUser = getCurrentUser();
			if (currentUser == null) {
				return "redirect:/login?redirect=/user/orders";
			}

			// 1. Bắt lỗi bắt buộc nhập lý do bằng Bean Validation (không bắt bằng HTML)
			if (errors.hasErrors()) {
				return "redirect:/user/orders?error=missing_reason";
			}

			boolean cancelled = orderServices.cancelOrder(id, currentUser.getId(), cancelBean.getReason());
			if (cancelled) {
				return "redirect:/user/orders?msg=cancel_success";
			} else {
				return "redirect:/user/orders?error=cancel_failed";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/orders?error=cancel_error";
		}
	}

	@PostMapping("/user/order/received/{id}")
	public String confirmReceivedOrder(@PathVariable("id") int id) {
		try {
			User currentUser = getCurrentUser();
			if (currentUser == null) {
				return "redirect:/login?redirect=/user/orders";
			}

			boolean confirmed = orderServices.confirmReceived(id, currentUser.getId());
			if (confirmed) {
				return "redirect:/user/orders?msg=received_success";
			} else {
				return "redirect:/user/orders?error=received_failed";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/user/orders?error=received_error";
		}
	}
}
