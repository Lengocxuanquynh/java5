package com.fpoly.java5demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.java5demo.beans.UserFormBean;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

	@Autowired
	private UserServices userServices;

	@GetMapping({ "/users", "/user-form" })
	public String userForm(Model model) {
		return "redirect:/admin?tab=users";
	}

	@GetMapping("/user-form/{id}")
	public String userEdit(@PathVariable("id") int id, Model model) {
		return "redirect:/admin?tab=users";
	}

	@PostMapping("/user-form")
	public String userSave(@Valid @ModelAttribute("bean") UserFormBean bean, Errors errors,
			Model model, RedirectAttributes redirectAttributes) {
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

		user.setUsername(bean.getUsername());
		if (bean.getPassword() != null && !bean.getPassword().trim().isEmpty()) {
			user.setPassword(bean.getPassword());
		}
		user.setName(bean.getName());
		user.setEmail(bean.getEmail());
		user.setPhone(bean.getPhone());
		user.setRole(bean.getRole());
		user.setStatus(bean.isActive());

		userServices.save(user);

		redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin người dùng thành công!");
		return "redirect:/admin?tab=users";
	}

	@GetMapping("/user/delete/{id}")
	public String userDelete(@PathVariable("id") int id, HttpServletRequest request, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentAdmin = userServices.getLoggedInUser(request, session);
		if (currentAdmin != null && currentAdmin.getId() == id) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự xóa tài khoản của chính mình!");
			return "redirect:/admin?tab=users";
		}

		User targetUser = userServices.getUserInfoById(id);
		if (targetUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản không tồn tại!");
			return "redirect:/admin?tab=users";
		}

		if (targetUser.getRole() == 1) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không được phép xóa tài khoản Quản trị viên (Admin)!");
			return "redirect:/admin?tab=users";
		}

		try {
			userServices.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản người dùng thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng này do có dữ liệu liên quan!");
		}
		return "redirect:/admin?tab=users";
	}

	@GetMapping("/user/toggle-status/{id}")
	public String userToggleStatus(@PathVariable("id") int id, HttpServletRequest request, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User currentAdmin = userServices.getLoggedInUser(request, session);
		if (currentAdmin != null && currentAdmin.getId() == id) {
			redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình!");
			return "redirect:/admin?tab=users";
		}

		User targetUser = userServices.getUserInfoById(id);
		if (targetUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản không tồn tại!");
			return "redirect:/admin?tab=users";
		}

		if (targetUser.getRole() == 1) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản Quản trị viên (Admin)!");
			return "redirect:/admin?tab=users";
		}

		targetUser.setStatus(!targetUser.isStatus());
		userServices.save(targetUser);

		redirectAttributes.addFlashAttribute("successMessage",
				(targetUser.isStatus() ? "Mở khóa" : "Khóa") + " tài khoản " + targetUser.getUsername() + " thành công!");
		return "redirect:/admin?tab=users";
	}
}
