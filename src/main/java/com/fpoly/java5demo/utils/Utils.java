package com.fpoly.java5demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Utils {
	private static final Pattern SIZE_PATTERN = Pattern.compile("^([\\d.]+)\\s*([a-zA-Z]+)$");
	public static final String COOKIE_KEY_USER_ID = "user_id";
	public static final String COOKIE_KEY_ROLE = "role";

	public static long parseToBytes(String input) {
		if (input == null || input.trim().isEmpty()) {
			return 0;
		}

		String normalizedInput = input.trim().toUpperCase();

		Matcher matcher = SIZE_PATTERN.matcher(normalizedInput);

		if (!matcher.find()) {
			throw new IllegalArgumentException("Định dạng kích thước không hợp lệ: " + input);
		}
		double value = Double.parseDouble(matcher.group(1));
		String unit = matcher.group(2);

		long multiplier = 1;

		switch (unit) {
		case "KB":
		case "K":
			multiplier = 1024L;
			break;
		case "MB":
		case "M":
			multiplier = 1024L * 1024;
			break;
		case "GB":
		case "G":
			multiplier = 1024L * 1024 * 1024;
			break;
		case "TB":
		case "T":
			multiplier = 1024L * 1024 * 1024 * 1024;
			break;
		case "B":
		case "BYTE":
		case "BYTES":
			multiplier = 1;
			break;
		default:
			throw new IllegalArgumentException("Đơn vị không được hỗ trợ: " + unit);
		}

		return (long) (value * multiplier);
	}

	public static String getCookieValue(String key, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(key)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public static void deleteCookie(String key, HttpServletResponse response) {
		if (response == null || key == null) {
			return;
		}
		Cookie cookie = new Cookie(key, "");
		cookie.setValue("");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	public static void clearCookie(HttpServletRequest request, HttpServletResponse response) {
		if (response == null) {
			return;
		}
		deleteCookie(COOKIE_KEY_USER_ID, response);
		deleteCookie(COOKIE_KEY_ROLE, response);

		if (request != null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					deleteCookie(cookie.getName(), response);
				}
			}
		}
	}

	public static void setCookie(String key, String value, HttpServletResponse response) {
		int day = 60 * 60 * 24 * 7;

		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(day);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
