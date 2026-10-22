package com.fpoly.java5demo.controllers;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.java5demo.beans.CartItemBean;
import com.fpoly.java5demo.beans.CheckoutBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.UserJPA;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.OrderServices;
import com.fpoly.java5demo.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

// [Vinh] Controller xử lý luồng bán hàng - code theo chuẩn thầy (Java5-SD21201)
// Dùng Cookie lấy userId, try-catch bắt lỗi, redirect khi thất bại
@Controller
public class OrderController {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private OrderServices orderServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserJPA userJPA;

	/**
	 * [Vinh] Trang checkout - điền thông tin đặt hàng
	 * Tự động kéo SĐT và Địa chỉ từ tài khoản user lên form (nhưng vẫn cho sửa)
	 */
	@GetMapping("/checkout")
	public String checkoutPage(@RequestParam(name = "items", required = false) List<Integer> items,
			Model model) {
		// Lấy user id từ cookie (theo chuẩn thầy)
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
			User currentUser = userJPA.findById(userId).orElse(null);

			if (currentUser == null) {
				return "redirect:/login?redirect=/checkout";
			}

			// Admin không được mua hàng
			if (currentUser.getRole() == 1) {
				return "redirect:/cart?error=admin_cannot_buy";
			}

			// Lấy danh sách sản phẩm trong giỏ (nếu có items được chọn thì lọc, không thì lấy hết)
			List<CartItemBean> cartItems;
			if (items != null && !items.isEmpty()) {
				cartItems = cartDetailServices.getCartItems(items);
			} else {
				cartItems = cartDetailServices.getCartItems();
			}

			if (cartItems.isEmpty()) {
				return "redirect:/cart";
			}

			int totalAmount = 0;
			int totalQuantity = 0;
			for (CartItemBean item : cartItems) {
				totalAmount += item.getSubTotal();
				totalQuantity += item.getQuantity();
			}

			// Tự động điền thông tin từ tài khoản user (theo chuẩn thầy)
			CheckoutBean bean = new CheckoutBean();
			bean.setFullName(currentUser.getName());
			bean.setPhone(currentUser.getPhone());
			bean.setAddress(currentUser.getAddress()); // [Vinh] Kéo địa chỉ mặc định từ user

			model.addAttribute("currentUser", currentUser);
			model.addAttribute("checkoutBean", bean);
			model.addAttribute("cartItems", cartItems);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("cartCount", totalQuantity);
			model.addAttribute("selectedItems", items);

			return "checkout";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/login?redirect=/checkout";
		}
	}

	/**
	 * [Vinh] Xử lý đặt hàng - POST
	 * Dùng try-catch theo chuẩn thầy
	 */
	@PostMapping("/checkout")
	public String placeOrder(@Valid @ModelAttribute("checkoutBean") CheckoutBean bean, Errors errors,
			@RequestParam(name = "items", required = false) List<Integer> items,
			Model model, RedirectAttributes redirectAttributes) {
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
			User currentUser = userJPA.findById(userId).orElse(null);

			if (currentUser == null) {
				return "redirect:/login?redirect=/checkout";
			}

			if (currentUser.getRole() == 1) {
				return "redirect:/cart?error=admin_cannot_buy";
			}

			List<CartItemBean> cartItems = (items != null && !items.isEmpty())
					? cartDetailServices.getCartItems(items)
					: cartDetailServices.getCartItems();

			if (cartItems.isEmpty()) {
				return "redirect:/cart";
			}

			int totalAmount = 0;
			int totalQuantity = 0;
			for (CartItemBean item : cartItems) {
				totalAmount += item.getSubTotal();
				totalQuantity += item.getQuantity();
			}

			if (errors.hasErrors()) {
				model.addAttribute("currentUser", currentUser);
				model.addAttribute("cartItems", cartItems);
				model.addAttribute("totalAmount", totalAmount);
				model.addAttribute("cartCount", totalQuantity);
				model.addAttribute("selectedItems", items);
				return "checkout";
			}

			// Tạo đơn hàng (theo chuẩn thầy - try-catch)
			int orderId = orderServices.createOrder(bean, items);

			return "redirect:/order/success/" + orderId;
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không thể hoàn tất: " + e.getMessage());
			return "redirect:/cart";
		}
	}

	@GetMapping("/order/success/{id}")
	public String orderSuccess(@PathVariable("id") int id, Model model) {
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
			User currentUser = userJPA.findById(userId).orElse(null);

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
	public String myOrders(Model model) {
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
			User currentUser = userJPA.findById(userId).orElse(null);

			if (currentUser == null) {
				return "redirect:/login?redirect=/user/orders";
			}

			// Admin được chuyển sang trang quản lý đơn hàng toàn sàn
			if (currentUser.getRole() == 1) {
				return "redirect:/admin/orders";
			}

			List<Order> orders = orderServices.getOrdersByUser(userId);
			model.addAttribute("orders", orders);
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
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));
			User currentUser = userJPA.findById(userId).orElse(null);

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
	public String cancelMyOrder(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));

			boolean success = orderServices.cancelOrder(id, userId);
			if (success) {
				redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng #" + id + " thành công!");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Không thể hủy đơn hàng! (Chỉ được hủy đơn khi ở trạng thái Chờ xác nhận)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi hủy đơn hàng!");
		}
		return "redirect:/user/orders";
	}

	@PostMapping("/user/order/received/{id}")
	public String confirmReceivedOrder(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
		try {
			int userId = Integer.parseInt(Utils.getCookieValue("user_id", request));

			boolean success = orderServices.confirmReceived(id, userId);
			if (success) {
				redirectAttributes.addFlashAttribute("successMessage",
						"Đã xác nhận nhận hàng cho đơn #" + id + " thành công!");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Không thể xác nhận! (Chỉ được xác nhận khi đơn ở trạng thái Đã giao hàng)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xác nhận nhận hàng!");
		}
		return "redirect:/user/orders";
	}
}
