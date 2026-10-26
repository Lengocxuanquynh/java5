package com.fpoly.java5demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.java5demo.beans.UserFormBean;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class AdminUserController {

	@Autowired
	private UserServices userServices;

	@GetMapping({ "/admin/users", "/admin/user-form" })
	public String userForm(Model model) {
		return "redirect:/admin?tab=users";
	}

	@GetMapping("/admin/user-form/{id}")
	public String userEdit(@PathVariable("id") int id, Model model) {
		return "redirect:/admin?tab=users";
	}

	@PostMapping("/admin/user-form")
	public String userSave(@Valid @ModelAttribute("bean") UserFormBean bean, Errors errors,
			Model model) {
		if (errors.hasErrors()) {
			return "redirect:/admin?tab=users";
		}

		User user;
		if (bean.getId() != null && bean.getId() > 0) {
			user = userServices.getUserInfoById(bean.getId());
			if (user == null) {
				user = new User();
			}
		} else {
			user = new User();
		}

		// [Vinh] Kiểm tra trùng SĐT khi thêm mới hoặc sửa - phục vụ luồng Order (ghi chú code đồng bộ)
		User existingPhone = userServices.findByPhone(bean.getPhone());
		if (existingPhone != null && (bean.getId() == null || existingPhone.getId() != bean.getId())) {
			return "redirect:/admin?tab=users";
		}

		user.setUsername(bean.getUsername());
		if (bean.getPassword() != null && !bean.getPassword().trim().isEmpty()) {
			user.setPassword(bean.getPassword());
		}
		user.setName(bean.getName());
		user.setEmail(bean.getEmail());
		user.setPhone(bean.getPhone());
		user.setAddress(bean.getAddress()); // [Vinh] Đồng bộ địa chỉ phục vụ luồng Order (code của bạn khác - ghi chú lại)
		user.setRole(bean.getRole());
		user.setStatus(bean.isActive());

		userServices.save(user);

		return "redirect:/admin?tab=users";
	}

	@PostMapping({ "/admin/user-toggle-status", "/admin/user/toggle-status" })
	public String userToggleStatus(@RequestParam("id") int id, HttpServletRequest request) {
		User currentAdmin = userServices.getLoggedInUser(request);
		if (currentAdmin != null && currentAdmin.getId() == id) {
			return "redirect:/admin?tab=users";
		}

		User targetUser = userServices.getUserInfoById(id);
		if (targetUser == null) {
			return "redirect:/admin?tab=users";
		}

		if (targetUser.getRole() == 1) {
			return "redirect:/admin?tab=users";
		}

		targetUser.setStatus(!targetUser.isStatus());
		userServices.save(targetUser);

		return "redirect:/admin?tab=users";
	}

	@GetMapping({ "/admin/user/toggle-status/{id}", "/admin/user-toggle-status/{id}" })
	public String userToggleStatusGet(@PathVariable("id") int id, HttpServletRequest request) {
		return userToggleStatus(id, request);
	}
}
