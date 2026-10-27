package com.fpoly.java5demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;

// [CartController] Quản lý giỏ hàng chuẩn phong cách thầy (Java5-SD21201)
// Chỉ dùng @GetMapping/@PostMapping, không dùng @RequestMapping ở class, không dùng RedirectAttributes
@Controller
public class CartController {

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	@GetMapping("/cart")
	public String viewCart(@RequestParam(name = "error", required = false) String error,
			@RequestParam(name = "msg", required = false) String msg,
			Model model, HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		if ("admin_cannot_buy".equals(error) || currentUser.getRole() == 1) {
			model.addAttribute("errorMessage", "Tài khoản Quản Trị Viên (Admin) không được phép mua hàng!");
		} else if ("out_of_stock".equals(error)) {
			model.addAttribute("errorMessage", "Số lượng sản phẩm trong kho không đủ!");
		} else if ("invalid_quantity".equals(error)) {
			model.addAttribute("errorMessage", "Số lượng sản phẩm không hợp lệ!");
		} else if ("max_cart_limit".equals(error)) {
			model.addAttribute("errorMessage", "Bạn đã có đủ số lượng sản phẩm này trong giỏ hàng (đạt giới hạn tối đa trong kho)!");
		} else if ("exceed_cart_limit".equals(error)) {
			model.addAttribute("errorMessage", "Số lượng thêm vào giỏ hàng vượt quá số lượng còn lại trong kho!");
		}

		if ("clear_success".equals(msg)) {
			model.addAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng!");
		} else if ("remove_success".equals(msg)) {
			model.addAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
		}

		model.addAttribute("currentUser", currentUser);
		model.addAttribute("cartItems", cartDetailServices.getCartItems(currentUser.getId()));
		model.addAttribute("totalAmount", cartDetailServices.getTotalAmount(currentUser.getId()));
		model.addAttribute("cartCount", cartDetailServices.getTotalQuantity(currentUser.getId()));
		return "cart";
	}

	@PostMapping("/cart/add")
	public String addToCart(@RequestParam("productId") int productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity,
			@RequestParam(name = "buyNow", defaultValue = "false") boolean buyNow,
			HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/product/" + productId;
		}

		if (currentUser.getRole() == 1) {
			return "redirect:/product/" + productId + "?error=admin_cannot_buy";
		}

		if (quantity <= 0) {
			return "redirect:/product/" + productId + "?error=invalid_quantity";
		}

		try {
			cartDetailServices.addToCart(currentUser.getId(), productId, quantity);
			if (buyNow) {
				return "redirect:/checkout?items=" + productId;
			}
			return "redirect:/product/" + productId + "?added=true";
		} catch (Exception e) {
			e.printStackTrace();
			String errorMsg = e.getMessage();
			if ("max_cart_limit".equals(errorMsg)) {
				return "redirect:/product/" + productId + "?error=max_cart_limit";
			} else if ("exceed_cart_limit".equals(errorMsg)) {
				return "redirect:/product/" + productId + "?error=exceed_cart_limit";
			}
			return "redirect:/product/" + productId + "?error=out_of_stock";
		}
	}

	@GetMapping("/cart/add/{id}")
	public String addToCartQuick(@PathVariable("id") int productId,
			HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		if (currentUser.getRole() == 1) {
			return "redirect:/product/" + productId + "?error=admin_cannot_buy";
		}

		try {
			cartDetailServices.addToCart(currentUser.getId(), productId, 1);
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/cart?error=out_of_stock";
		}
		return "redirect:/cart";
	}

	@PostMapping("/cart/update")
	public String updateQuantity(@RequestParam("productId") int productId,
			@RequestParam("quantity") int quantity,
			HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.updateQuantity(currentUser.getId(), productId, quantity);
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/cart?error=out_of_stock";
		}
		return "redirect:/cart";
	}

	@GetMapping("/cart/remove/{id}")
	public String removeFromCartGet(@PathVariable("id") int productId,
			HttpServletRequest request) {
		return removeFromCart(productId, request);
	}

	@PostMapping("/cart/remove")
	public String removeFromCart(@RequestParam("id") int productId,
			HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.removeCart(currentUser.getId(), productId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/cart?msg=remove_success";
	}

	@GetMapping("/cart/clear")
	public String clearCartGet(HttpServletRequest request) {
		return clearCart(request);
	}

	@PostMapping("/cart/clear")
	public String clearCart(HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.clearCart(currentUser.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/cart?msg=clear_success";
	}
}
