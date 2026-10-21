package com.fpoly.java5demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fpoly.java5demo.entities.Image;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.jpas.ImageJPA;
import com.fpoly.java5demo.jpas.ProductJPA;

@Service
public class ProductServices {

	@Autowired
	private ProductJPA productJPA;

	@Autowired
	private ImageJPA imageJPA;

	@Autowired
	private ImageServices imageService;

	public List<Product> getList() {
		try {
			return productJPA.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<Product> getAvailableProducts() {
		try {
			return productJPA.findByStatusTrue();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<Product> getByCategory(int catId) {
		try {
			return productJPA.findByCategoryIdAndStatusTrue(catId);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public List<Product> search(String keyword) {
		try {
			if (keyword == null || keyword.trim().isEmpty()) {
				return getAvailableProducts();
			}
			return productJPA.findByNameContainingIgnoreCaseAndStatusTrue(keyword.trim());
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	public String addProduct(Product product, List<MultipartFile> images) {
		try {
			if (product.getName() == null || product.getName().trim().isEmpty()) {
				return "Tên sản phẩm không được để trống";
			}
			product.setName(product.getName().trim());

			// Kiểm tra tên sản phẩm đã tồn tại chưa bằng Optional
			Optional<Product> prodOptional = productJPA.checkNameExist(product.getName());
			if (prodOptional.isPresent()) {
				return "Trùng tên";
			}

			Product prodSave = productJPA.save(product);

			if (images != null) {
				for (MultipartFile image : images) {
					if (image != null && !image.isEmpty()) {
						String name = imageService.save(image);
						if (name != null) {
							Image imageSave = new Image();
							imageSave.setName(name);
							imageSave.setProduct(prodSave);
							imageJPA.save(imageSave);
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return "Lỗi thêm sản phẩm: " + e.getMessage();
		}
	}

	public String updateProduct(Product product, List<MultipartFile> images) {
		try {
			if (product.getName() == null || product.getName().trim().isEmpty()) {
				return "Tên sản phẩm không được để trống";
			}
			product.setName(product.getName().trim());

			// Kiểm tra tên sản phẩm đã tồn tại chưa (ngoại trừ sản phẩm đang sửa) bằng Optional
			Optional<Product> prodOptional = productJPA.checkNameExist(product.getName(), product.getId());
			if (prodOptional.isPresent()) {
				return "Trùng tên";
			}

			Product prodSave = productJPA.save(product);

			if (images != null && !images.isEmpty()) {
				boolean hasFile = false;
				for (MultipartFile file : images) {
					if (file != null && !file.isEmpty()) {
						hasFile = true;
						break;
					}
				}
				if (hasFile) {
					// Xoá ảnh cũ nếu có ảnh mới
					List<Image> oldImages = imageJPA.findByProductId(prodSave.getId());
					for (Image img : oldImages) {
						imageJPA.delete(img);
					}

					for (MultipartFile image : images) {
						if (image != null && !image.isEmpty()) {
							String name = imageService.save(image);
							if (name != null) {
								Image imageSave = new Image();
								imageSave.setName(name);
								imageSave.setProduct(prodSave);
								imageJPA.save(imageSave);
							}
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return "Lỗi cập nhật sản phẩm: " + e.getMessage();
		}
	}

	public String remove(int id) {
		try {
			Optional<Product> prodOptional = productJPA.findById(id);
			if (prodOptional.isPresent()) {
				Product product = prodOptional.get();
				if (product.getCartDetails() != null && product.getCartDetails().size() > 0) {
					product.setStatus(false);
					productJPA.save(product);
				} else {
					if (product.getImages() != null) {
						for (Image image : product.getImages()) {
							imageJPA.delete(image);
						}
					}
					productJPA.delete(product);
				}
				return null;
			}
			return "Không tìm thấy sản phẩm";
		} catch (Exception e) {
			e.printStackTrace();
			return "Lỗi xóa sản phẩm: " + e.getMessage();
		}
	}

	public Product findById(int id) {
		return productJPA.findById(id).orElse(null);
	}
}
