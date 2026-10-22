package com.fpoly.java5demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.java5demo.beans.ChangePasswordBean;
import com.fpoly.java5demo.beans.ProfileBean;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserServices userServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@GetMapping("/profile")
	public String profile(Model model, HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/user/profile";
		}

		if (!model.containsAttribute("profileBean")) {
			ProfileBean profileBean = new ProfileBean();
			profileBean.setId(currentUser.getId());
			profileBean.setUsername(currentUser.getUsername());
			profileBean.setName(currentUser.getName());
			profileBean.setEmail(currentUser.getEmail());
			profileBean.setPhone(currentUser.getPhone());
			profileBean.setAddress(currentUser.getAddress()); // [Vinh] Đồng bộ địa chỉ phục vụ luồng Order
			model.addAttribute("profileBean", profileBean);
		}

		if (!model.containsAttribute("passwordBean")) {
			ChangePasswordBean pwdBean = new ChangePasswordBean();
			model.addAttribute("passwordBean", pwdBean);
			model.addAttribute("changePasswordBean", pwdBean);
		}

		model.addAttribute("currentUser", currentUser);
		model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
		return "user/profile";
	}

	@PostMapping({ "/profile", "/profile/update" })
	public String updateProfile(@Valid @ModelAttribute("profileBean") ProfileBean profileBean,
			Errors errors, Model model, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/user/profile";
		}

		if (errors.hasErrors()) {
			model.addAttribute("currentUser", currentUser);
			ChangePasswordBean pwdBean = new ChangePasswordBean();
			model.addAttribute("passwordBean", pwdBean);
			model.addAttribute("changePasswordBean", pwdBean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("activeTab", "profile");
			return "user/profile";
		}

		// Kiểm tra trùng email với user khác
		User existingUser = userServices.findByEmail(profileBean.getEmail());
		if (existingUser != null && existingUser.getId() != currentUser.getId()) {
			model.addAttribute("currentUser", currentUser);
			ChangePasswordBean pwdBean = new ChangePasswordBean();
			model.addAttribute("passwordBean", pwdBean);
			model.addAttribute("changePasswordBean", pwdBean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("emailError", "Email này đã được sử dụng bởi một tài khoản khác!");
			model.addAttribute("activeTab", "profile");
			return "user/profile";
		}

		// [Vinh] Kiểm tra trùng SĐT với user khác - phục vụ luồng Order
		User existingPhone = userServices.findByPhone(profileBean.getPhone());
		if (existingPhone != null && existingPhone.getId() != currentUser.getId()) {
			model.addAttribute("currentUser", currentUser);
			ChangePasswordBean pwdBean = new ChangePasswordBean();
			model.addAttribute("passwordBean", pwdBean);
			model.addAttribute("changePasswordBean", pwdBean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("phoneError", "Số điện thoại này đã được sử dụng bởi một tài khoản khác!");
			model.addAttribute("activeTab", "profile");
			return "user/profile";
		}

		currentUser.setName(profileBean.getName());
		currentUser.setEmail(profileBean.getEmail());
		currentUser.setPhone(profileBean.getPhone());
		currentUser.setAddress(profileBean.getAddress()); // [Vinh] Lưu địa chỉ vào DB phục vụ luồng Order
		userServices.save(currentUser);

		redirectAttributes.addFlashAttribute("successProfileMessage", "Cập nhật hồ sơ tài khoản thành công!");
		redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ tài khoản thành công!");
		return "redirect:/user/profile";
	}

	@PostMapping({ "/change-password", "/profile/change-password" })
	public String changePassword(@Valid @ModelAttribute("passwordBean") ChangePasswordBean bean,
			Errors errors, Model model, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/user/profile";
		}

		String currentPwd = bean.getCurrentPassword() != null ? bean.getCurrentPassword() : bean.getOldPassword();

		if (errors.hasErrors()) {
			ProfileBean profileBean = new ProfileBean();
			profileBean.setId(currentUser.getId());
			profileBean.setUsername(currentUser.getUsername());
			profileBean.setName(currentUser.getName());
			profileBean.setEmail(currentUser.getEmail());
			profileBean.setPhone(currentUser.getPhone());
			profileBean.setAddress(currentUser.getAddress()); // [Vinh]

			model.addAttribute("currentUser", currentUser);
			model.addAttribute("profileBean", profileBean);
			model.addAttribute("changePasswordBean", bean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("activeTab", "password");
			return "user/profile";
		}

		if (!currentUser.getPassword().equals(currentPwd)) {
			ProfileBean profileBean = new ProfileBean();
			profileBean.setId(currentUser.getId());
			profileBean.setUsername(currentUser.getUsername());
			profileBean.setName(currentUser.getName());
			profileBean.setEmail(currentUser.getEmail());
			profileBean.setPhone(currentUser.getPhone());
			profileBean.setAddress(currentUser.getAddress()); // [Vinh]

			errors.rejectValue("currentPassword", "mismatch", "Mật khẩu hiện tại không chính xác!");
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("profileBean", profileBean);
			model.addAttribute("changePasswordBean", bean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("passwordError", "Mật khẩu hiện tại không chính xác!");
			model.addAttribute("activeTab", "password");
			return "user/profile";
		}

		if (!bean.getNewPassword().equals(bean.getConfirmPassword())) {
			ProfileBean profileBean = new ProfileBean();
			profileBean.setId(currentUser.getId());
			profileBean.setUsername(currentUser.getUsername());
			profileBean.setName(currentUser.getName());
			profileBean.setEmail(currentUser.getEmail());
			profileBean.setPhone(currentUser.getPhone());
			profileBean.setAddress(currentUser.getAddress()); // [Vinh]

			errors.rejectValue("confirmPassword", "mismatch", "Xác nhận mật khẩu mới không khớp!");
			model.addAttribute("currentUser", currentUser);
			model.addAttribute("profileBean", profileBean);
			model.addAttribute("changePasswordBean", bean);
			model.addAttribute("cartCount", cartDetailServices.getTotalQuantity());
			model.addAttribute("passwordError", "Xác nhận mật khẩu mới không khớp!");
			model.addAttribute("activeTab", "password");
			return "user/profile";
		}

		currentUser.setPassword(bean.getNewPassword());
		userServices.save(currentUser);

		redirectAttributes.addFlashAttribute("successPasswordMessage", "Đổi mật khẩu thành công!");
		redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
		return "redirect:/user/profile";
	}
}
