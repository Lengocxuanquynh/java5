package com.fpoly.java5demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.validation.Errors;
import jakarta.validation.Valid;
import com.fpoly.java5demo.beans.CancelOrderBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.services.OrderServices;

@Controller
public class AdminOrderController {

	@Autowired
	private OrderServices orderServices;

	@GetMapping("/admin/orders")
	public String listOrders(Model model) {
		return "redirect:/admin?tab=orders";
	}

	@GetMapping("/admin/order/{id}")
	public String viewOrder(@PathVariable("id") int id, Model model) {
		Order order = orderServices.getOrderById(id);
		if (order == null) {
			return "redirect:/admin?tab=orders";
		}
		List<OrderDetail> details = orderServices.getOrderDetails(id);
		model.addAttribute("order", order);
		model.addAttribute("orderDetails", details);
		return "admin/order-detail";
	}

	@PostMapping("/admin/order/status")
	public String updateStatus(@RequestParam("orderId") int orderId,
			@RequestParam("status") int status,
			@Valid @ModelAttribute("cancelBean") CancelOrderBean cancelBean, Errors errors) {
		// Bắt lỗi bằng Bean Validation khi Admin chọn Hủy đơn (status 6)
		if (status == 6 && errors.hasErrors()) {
			return "redirect:/admin/order/" + orderId;
		}

		orderServices.updateStatus(orderId, status, cancelBean != null ? cancelBean.getReason() : null);
		return "redirect:/admin/order/" + orderId;
	}
}
