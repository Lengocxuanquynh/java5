package com.fpoly.java5demo.validation;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fpoly.java5demo.utils.Utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageFileValidator implements ConstraintValidator<ValidImage, List<MultipartFile>> {

	private String[] types;
	private long maxSize;

	@Override
	public void initialize(ValidImage constraintAnnotation) {
		this.types = constraintAnnotation.type();
		this.maxSize = Utils.parseToBytes(constraintAnnotation.maxSize());
	}

//	isValid => true => Không lỗi
//	isValid => false => Có lỗi 
	@Override
	public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
		if (files == null || files.isEmpty()) {
			return true;
		}

		for (MultipartFile file : files) {
			// Kiểm tra rỗng
			if (file.isEmpty()) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("File không được rỗng").addConstraintViolation();
				return false;
			}

			// Kiểm tra định dạng (Type)
			String contentType = file.getContentType();
			if (!Arrays.asList(types).contains(contentType)) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(
						"Chỉ chấp nhận ảnh (JPG, PNG). File sai: " + file.getOriginalFilename())
						.addConstraintViolation();
				return false;
			}

			// Kiểm tra kích thước (Size)
			if (file.getSize() > maxSize) {
				context.disableDefaultConstraintViolation();
				// Chuyển đổi byte sang MB để hiển thị thông báo cho dễ đọc
				context.buildConstraintViolationWithTemplate("File quá lớn: " + file.getOriginalFilename())
						.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}