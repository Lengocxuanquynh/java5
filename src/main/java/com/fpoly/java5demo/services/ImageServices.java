package com.fpoly.java5demo.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServices {

	private String uploadDir = "uploads";

	public String save(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		// Kiểm tra loại file
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new RuntimeException("Only image files are allowed");
		}

		try {
			// Tạo thư mục nếu chưa tồn tại
			Files.createDirectories(Paths.get(uploadDir));

			String originalName = file.getOriginalFilename();
			String extension = originalName.substring(originalName.lastIndexOf("."));

			String fileName = String.format("%d%s", new Date().getTime(), extension);
			Path targetPath = Paths.get(uploadDir, fileName);

			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

			return fileName;

		} catch (IOException e) {
			throw new RuntimeException("Cannot store image file", e);
		}
	}
}
