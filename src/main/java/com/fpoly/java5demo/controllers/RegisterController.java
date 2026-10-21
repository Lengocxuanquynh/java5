package com.fpoly.java5demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegisterController {

	@GetMapping("/register-redirect")
	public String registerUI() {
		return "redirect:/register";
	}
}
