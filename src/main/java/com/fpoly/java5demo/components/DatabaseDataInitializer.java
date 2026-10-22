package com.fpoly.java5demo.components;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.OrderDetail;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.jpas.CategoryJPA;
import com.fpoly.java5demo.jpas.OrderDetailJPA;
import com.fpoly.java5demo.jpas.OrderJPA;
import com.fpoly.java5demo.jpas.ProductJPA;
import com.fpoly.java5demo.jpas.UserJPA;

@Component
public class DatabaseDataInitializer implements CommandLineRunner {

	@Autowired
	private UserJPA userJPA;

	@Autowired
	private CategoryJPA categoryJPA;

	@Autowired
	private ProductJPA productJPA;

	@Autowired
	private OrderJPA orderJPA;

	@Autowired
	private OrderDetailJPA orderDetailJPA;

	@Override
	public void run(String... args) throws Exception {
		// 1. Khởi tạo tài khoản mẫu
		if (userJPA.count() == 0) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword("123456");
			admin.setName("Quản Trị Viên Shopee");
			admin.setEmail("admin@shopee.vn");
			admin.setPhone("0987654321");
			admin.setAddress("Trụ sở PolyShop, Tòa nhà FPT Polytechnic, Phố Trịnh Văn Bô, Nam Từ Liêm, Hà Nội");
			admin.setRole(1); // 1: Admin
			admin.setStatus(true);
			userJPA.save(admin);

			User user = new User();
			user.setUsername("user1");
			user.setPassword("123456");
			user.setName("Nguyễn Văn A");
			user.setEmail("nguyenvana@gmail.com");
			user.setPhone("0912345678");
			user.setAddress("Tòa FPT Polytechnic, Phố Trịnh Văn Bô, Nam Từ Liêm, Hà Nội");
			user.setRole(0); // 0: User
			user.setStatus(true);
			userJPA.save(user);

			User user2 = new User();
			user2.setUsername("user2");
			user2.setPassword("123456");
			user2.setName("Trần Thị Bích");
			user2.setEmail("bichthitran@gmail.com");
			user2.setPhone("0988776655");
			user2.setAddress("Số 123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
			user2.setRole(0); // 0: User
			user2.setStatus(true);
			userJPA.save(user2);

			System.out.println(">>> Đã khởi tạo tài khoản mẫu: admin, user1, user2 (Mật khẩu: 123456)");
		}

		// Tự động tìm và xóa danh mục "Thời Trang Nam", "Thời Trang Nữ" nếu đã tồn tại
		// trong CSDL
		try {
			List<Category> fashionCats = categoryJPA.findAll().stream()
					.filter(c -> c.getName() != null && (c.getName().equalsIgnoreCase("Thời Trang Nam")
							|| c.getName().equalsIgnoreCase("Thời Trang Nữ")))
					.toList();

			for (Category cat : fashionCats) {
				List<Product> prods = productJPA.findAll().stream()
						.filter(p -> p.getCategory() != null && p.getCategory().getId() == cat.getId())
						.toList();
				if (!prods.isEmpty()) {
					productJPA.deleteAll(prods);
				}
				categoryJPA.delete(cat);
				System.out.println(">>> Đã xóa danh mục và sản phẩm của: " + cat.getName());
			}
		} catch (Exception e) {
			System.err.println("Lỗi khi dọn dẹp danh mục thời trang: " + e.getMessage());
		}

		// 2. Khởi tạo danh mục sản phẩm nếu chưa có
		if (categoryJPA.count() == 0) {
			Category cat1 = new Category();
			cat1.setName("Điện Thoại & Phụ Kiện");

			Category cat2 = new Category();
			cat2.setName("Máy Tính & Laptop");

			Category cat3 = new Category();
			cat3.setName("Thiết Bị Điện Tử");

			List<Category> savedCategories = categoryJPA.saveAll(Arrays.asList(cat1, cat2, cat3));
			System.out.println(">>> Đã khởi tạo 3 danh mục sản phẩm PolyShop");

			// 3. Khởi tạo sản phẩm mẫu
			if (productJPA.count() == 0) {
				Category c1 = savedCategories.get(0); // Điện thoại & Phụ Kiện
				Category c2 = savedCategories.get(1); // Máy Tính & Laptop
				Category c3 = savedCategories.get(2); // Thiết Bị Điện Tử

				List<Product> sampleProducts = Arrays.asList(
						new Product(0, "Điện thoại Apple iPhone 15 Pro Max 256GB Titan Tự Nhiên", 29490000, 35,
								true, c1, null, null),
						new Product(0, "Tai nghe không dây Bluetooth True Wireless chống ồn chủ động", 350000, 150,
								true, c1, null, null),
						new Product(0, "Củ sạc nhanh 20W Type-C chuẩn PD cho iPhone / iPad", 180000, 200, true, c1,
								null, null),
						new Product(0, "Laptop Apple MacBook Air M2 13.6 inch 8GB RAM 256GB SSD", 23990000, 15, true,
								c2, null, null),
						new Product(0, "Chuột máy tính không dây công thái học Silent Click", 220000, 80, true, c2,
								null, null),
						new Product(0, "Bàn phím cơ không dây Bluetooth RGB Led 87 phím Hot-swap", 750000, 50, true,
								c2, null, null),
						new Product(0, "Loa Bluetooth không dây mini âm bass siêu trầm chống nước", 290000, 120,
								true, c3, null, null),
						new Product(0, "Đồng hồ thông minh Smartwatch theo dõi nhịp tim chống nước", 680000, 60,
								true, c3, null, null));

				productJPA.saveAll(sampleProducts);
				System.out.println(">>> Đã khởi tạo 8 sản phẩm mẫu công nghệ PolyShop");
			}
		}

		// 4. Khởi tạo đơn hàng mẫu nếu chưa có
		if (orderJPA.count() == 0) {
			User user1 = userJPA.findByUsername("user1").orElse(null);
			User admin = userJPA.findByUsername("admin").orElse(null);
			List<Product> prods = productJPA.findAll();

			if (user1 != null && prods.size() >= 3) {
				// Đơn 1: user1 - Đã nhận hàng (status = 5: Đã nhận hàng / Hoàn thành)
				Order o1 = new Order();
				o1.setUser(user1);
				o1.setShippingName("Nguyễn Văn A");
				o1.setShippingPhone("0912345678");
				o1.setShippingAddress("Tòa FPT Polytechnic, Phố Trịnh Văn Bô, Nam Từ Liêm, Hà Nội");
				o1.setNote("Giao giờ hành chính");
				o1.setDate(Date.valueOf("2026-10-18"));
				o1.setStatus(5); // 5: Đã nhận hàng / Hoàn thành
				o1.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis() - 86400000L));
				int p1Price = prods.get(1).getSalePrice();
				int p2Price = prods.get(2).getSalePrice();
				o1.setTotalPrice(p1Price + p2Price);
				Order savedO1 = orderJPA.save(o1);

				OrderDetail od1_1 = new OrderDetail(0, p1Price, 1, savedO1, prods.get(1));
				OrderDetail od1_2 = new OrderDetail(0, p2Price, 1, savedO1, prods.get(2));
				orderDetailJPA.saveAll(Arrays.asList(od1_1, od1_2));

				// Đơn 2: user1 - Đã giao hàng (status = 4: Đã giao hàng) -> Có nút "Đã Nhận Hàng"
				Order o2 = new Order();
				o2.setUser(user1);
				o2.setShippingName("Nguyễn Văn A");
				o2.setShippingPhone("0912345678");
				o2.setShippingAddress("Số 45 Lê Duẩn, Quận 1, TP. Hồ Chí Minh");
				o2.setNote("Gọi trước khi giao 15 phút");
				o2.setDate(Date.valueOf("2026-10-19"));
				o2.setStatus(4); // 4: Đã giao hàng (chờ khách bấm xác nhận)
				o2.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis() - 3600000L));
				int p6Price = prods.get(6).getSalePrice();
				o2.setTotalPrice(p6Price);
				Order savedO2 = orderJPA.save(o2);

				OrderDetail od2_1 = new OrderDetail(0, p6Price, 1, savedO2, prods.get(6));
				orderDetailJPA.save(od2_1);

				// Đơn 3: user2 (Trần Thị Bích) - Chờ xác nhận (status = 1) -> Có nút "Hủy Đơn"
				User user2 = userJPA.findByUsername("user2").orElse(user1);
				if (user2 != null) {
					Order o3 = new Order();
					o3.setUser(user2);
					o3.setShippingName("Trần Thị Bích");
					o3.setShippingPhone("0988776655");
					o3.setShippingAddress("123 Đường Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
					o3.setNote("Để ở quầy lễ tân");
					o3.setDate(Date.valueOf("2026-10-20"));
					o3.setStatus(1); // 1: Chờ xác nhận
					o3.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
					int p4Price = prods.get(4).getSalePrice();
					o3.setTotalPrice(p4Price);
					Order savedO3 = orderJPA.save(o3);

					OrderDetail od3_1 = new OrderDetail(0, p4Price, 1, savedO3, prods.get(4));
					orderDetailJPA.save(od3_1);
				}

				System.out.println(">>> Đã khởi tạo 3 đơn hàng mẫu PolyShop kèm chi tiết đơn hàng (status 1-6 chuẩn)");
			}
		}
	}
}
