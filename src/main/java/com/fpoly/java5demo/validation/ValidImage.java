package com.fpoly.java5demo.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {
	String message() default "File ảnh không hợp lệ";

	String[] type() default { "image/jpeg", "image/png", "image/jpg" };

	String maxSize() default "5MB";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}