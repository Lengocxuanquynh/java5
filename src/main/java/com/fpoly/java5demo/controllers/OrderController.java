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
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.OrderServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class OrderController {

	@Autowired
	private OrderServices orderServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	@GetMapping("/checkout")
	public String checkoutPage(@RequestParam(name = "items", required = false) List<Integer> items,
			Model model, HttpServletRequest request, HttpSession session) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser == null) {
			return "redirect:/login?redirect=/checkout";
		}

		if (currentUser.getRole() == 1) {
			return "redirect:/cart?error=admin_cannot_buy";
		}

		List<CartItemBean> cartItems;
		if (items != null && !items.isEmpty()) {
			cartItems = cartDetailServices.getCartItems(items);
			session.setAttribute("selectedCheckoutItemIds", items);
		} else {
			@SuppressWarnings("unchecked")
			List<Integer> sessionItems = (List<Integer>) session.getAttribute("selectedCheckoutItemIds");
			if (sessionItems != null && !sessionItems.isEmpty()) {
				cartItems = cartDetailServices.getCartItems(sessionItems);
			} else {
				cartItems = cartDetailServices.getCartItems();
			}
		}

		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}

		int totalAmount = cartItems.stream().mapToInt(CartItemBean::getSubTotal).sum();
		int totalQuantity = cartItems.stream().mapToInt(CartItemBean::getQuantity).sum();

		// Lưu tổng tiền hiển thị trên màn hình vào session (không dùng hidden ở HTML)
		session.setAttribute("checkoutExpectedTotal", totalAmount);

		CheckoutBean bean = new CheckoutBean();
		bean.setFullName(currentUser.getName());
		bean.setPhone(currentUser.getPhone());

		model.addAttribute("currentUser", currentUser);
		model.addAttribute("checkoutBean", bean);
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("cartCount", totalQuantity);

		return "checkout";
	}

	@PostMapping("/checkout")
	public String placeOrder(@Valid @ModelAttribute("checkoutBean") CheckoutBean bean, Errors errors,
			Model model, HttpServletRequest request, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser == null) {
			return "redirect:/login?redirect=/checkout";
		}

		if (currentUser.getRole() == 1) {
			return "redirect:/cart?error=admin_cannot_buy";
		}

		@SuppressWarnings("unchecked")
		List<Integer> selectedItems = (List<Integer>) session.getAttribute("selectedCheckoutItemIds");
		List<CartItemBean> cartItems = (selectedItems != null && !selectedItems.isEmpty())
				? cartDetailServices.getCartItems(selectedItems)
				: cartDetailServices.getCartItems();

		if (cartItems.isEmpty()) {
			return "redirect:/cart";
		}

		int totalAmount = cartItems.stream().mapToInt(CartItemBean::getSubTotal).sum();
		int totalQuantity = cartItems.stream().mapToInt(CartItemBean::getQuantity).sum();

		// So sánh tổng tiền vừa tính lại từ DB với tổng tiền lúc hiển thị trên màn hình client
		Integer expectedTotal = (Integer) session.getAttribute("checkoutExpectedTotal");
		if (expectedTotal == null || !expectedTotal.equals(totalAmount)) {
			session.removeAttribute("checkoutExpectedTotal");
			redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không thể hoàn tất");
			return "redirect:/cart";
		}

		if (errors.hasErrors()) {
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("cartItems", cartItems);
			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("cartCount", totalQuantity);
			return "checkout";
		}

		Order order = orderServices.createOrder(currentUser, bean, selectedItems);
		if (order == null) {
			session.removeAttribute("checkoutExpectedTotal");
			redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không thể hoàn tất");
			return "redirect:/cart";
		}

		session.removeAttribute("selectedCheckoutItemIds");
		session.removeAttribute("checkoutExpectedTotal");

		return "redirect:/order/success/" + order.getId();
	}

	@GetMapping("/order/success/{id}")
	public String orderSuccess(@PathVariable("id") int id, Model model, HttpServletRequest request,
			HttpSession session) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser == null) {
			return "redirect:/login";
		}

		Order order = orderServices.getOrderById(id);
		if (order == null || (order.getUser() != null && order.getUser().getId() != currentUser.getId()
				&& currentUser.getRole() != 1)) {
			return "redirect:/";
		}

		List<OrderDetail> details = orderServices.getOrderDetails(id);
		model.addAttribute("order", order);
		model.addAttribute("orderDetails", details);
		model.addAttribute("currentUser", currentUser);
		return "order-success";
	}

	@GetMapping("/user/orders")
	public String myOrders(Model model, HttpServletRequest request, HttpSession session) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser == null) {
			return "redirect:/login?redirect=/user/orders";
		}

		// Tài khoản Admin được chuyển sang trang Quản lý đơn hàng toàn sàn
		if (currentUser.getRole() == 1) {
			return "redirect:/admin/orders";
		}

		List<Order> orders = orderServices.getOrdersByUser(currentUser.getId());
		model.addAttribute("orders", orders);
		model.addAttribute("currentUser", currentUser);
		model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
		return "user/orders";
	}

	@GetMapping("/user/order/{id}")
	public String myOrderDetail(@PathVariable("id") int id, Model model, HttpServletRequest request,
			HttpSession session) {
		User currentUser = userServices.getLoggedInUser(request, session);
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
	}

	@PostMapping("/user/order/cancel/{id}")
	public String cancelMyOrder(@PathVariable("id") int id, HttpServletRequest request, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser != null) {
			boolean success = orderServices.cancelOrder(id, currentUser.getId());
			if (success) {
				redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng #" + id + " thành công!");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Không thể hủy đơn hàng! (Chỉ được hủy đơn khi ở trạng thái Chờ xác nhận)");
			}
		}
		return "redirect:/user/orders";
	}

	@PostMapping("/user/order/received/{id}")
	public String confirmReceivedOrder(@PathVariable("id") int id, HttpServletRequest request, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request, session);
		if (currentUser != null) {
			boolean success = orderServices.confirmReceived(id, currentUser.getId());
			if (success) {
				redirectAttributes.addFlashAttribute("successMessage",
						"Đã xác nhận nhận hàng cho đơn #" + id + " thành công!");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Không thể xác nhận! (Chỉ được xác nhận khi đơn ở trạng thái Đã giao hàng)");
			}
		}
		return "redirect:/user/orders";
	}
}
