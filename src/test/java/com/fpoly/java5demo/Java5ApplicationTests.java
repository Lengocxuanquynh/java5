package com.fpoly.java5demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import com.fpoly.java5demo.beans.CheckoutBean;
import com.fpoly.java5demo.entities.Order;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.OrderServices;
import com.fpoly.java5demo.services.UserServices;

import java.util.List;

@SpringBootTest
class Java5ApplicationTests {

	@Autowired
	private OrderServices orderServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	@Test
	void testOrderCreationAndRetrieval() {
		User admin = userServices.getUserInfoById(1); // admin (role = 1)
		assertNotNull(admin);
		assertEquals(1, admin.getRole());

		User user = userServices.getUserInfoById(2); // user1 (role = 0)
		assertNotNull(user);
		assertEquals(0, user.getRole());

		// Add to cart
		cartDetailServices.addToCart(1, 1);
		assertEquals(1, cartDetailServices.getTotalQuantity());

		CheckoutBean bean = new CheckoutBean();
		bean.setFullName("Nguyen Van Test");
		bean.setPhone("0987654321");
		bean.setAddress("123 Test Street, Hanoi");
		bean.setNote("Giao gio hanh chinh");

		// 1. Verify Admin CANNOT place order
		Order adminOrder = orderServices.createOrder(admin, bean);
		assertNull(adminOrder, "Admin must NOT be allowed to buy products!");

		// 2. Verify Regular User CAN place order
		Order order = orderServices.createOrder(user, bean);
		assertNotNull(order, "Regular user must be able to create orders!");
		assertTrue(order.getId() > 0);
		assertEquals(1, order.getStatus(), "Đơn hàng mới tạo phải có status = 1 (Chờ xác nhận)");
		assertNotNull(order.getUpdatedAt(), "Trường updated_at phải được khởi tạo");

		// Check orders by user
		List<Order> orders = orderServices.getOrdersByUser(user.getId());
		assertFalse(orders.isEmpty());
		assertEquals(order.getId(), orders.get(0).getId());
		assertNotNull(orders.get(0).getOrderDetails());
		System.out.println(">>> orderDetails size: " + orders.get(0).getOrderDetails().size());
		assertFalse(orders.get(0).getOrderDetails().isEmpty());

		// 3. Test Teacher's 1-way status transitions: 1 -> 2 -> 3 -> 4
		assertFalse(orderServices.updateStatus(order.getId(), 3), "Không được nhảy cóc từ 1 sang 3");
		assertFalse(orderServices.updateStatus(order.getId(), 4), "Không được nhảy cóc từ 1 sang 4");
		assertTrue(orderServices.updateStatus(order.getId(), 2), "Admin chuyển 1 -> 2 (Đã xác nhận) thành công");
		assertTrue(orderServices.updateStatus(order.getId(), 3), "Admin chuyển 2 -> 3 (Đang giao hàng) thành công");
		assertTrue(orderServices.updateStatus(order.getId(), 4), "Admin chuyển 3 -> 4 (Đã giao hàng) thành công");

		// 4. Test Teacher's confirm received rule: Only allowed when status == 4
		assertTrue(orderServices.confirmReceived(order.getId(), user.getId()),
				"User xác nhận nhận hàng khi status == 4 -> status 5 thành công");
		Order confirmedOrder = orderServices.getOrderById(order.getId());
		assertEquals(5, confirmedOrder.getStatus(), "Trạng thái sau khi khách xác nhận phải là 5");
		assertNotNull(confirmedOrder.getUpdatedAt(), "updated_at phải được cập nhật");

		// Test revenue and order count
		long orderCount = orderServices.getOrderCount();
		assertTrue(orderCount > 0);

		long completedRevenue = orderServices.getCompletedRevenue();
		assertTrue(completedRevenue > 0);
		System.out.println(">>> completedRevenue: " + completedRevenue + ", orderCount: " + orderCount);
	}

	@Autowired
	private com.fpoly.java5demo.jpas.CategoryJPA categoryJPA;

	@Autowired
	private com.fpoly.java5demo.services.CategoryServices categoryServices;

	@Autowired
	private com.fpoly.java5demo.jpas.ProductJPA productJPA;

	@Autowired
	private com.fpoly.java5demo.services.ProductServices productServices;

	@Test
	void testCategoryDuplicateValidation() {
		// 1. Kiểm tra tồn tại bằng Optional
		java.util.Optional<com.fpoly.java5demo.entities.Category> catOpt = categoryJPA
				.checkNameExist("Điện Thoại & Phụ Kiện");
		assertTrue(catOpt.isPresent(), "Category JPA Optional checkNameExist phải tìm thấy");

		// Kiểm tra không phân biệt hoa thường và khoảng trắng
		java.util.Optional<com.fpoly.java5demo.entities.Category> catOptCase = categoryJPA
				.checkNameExist("  điện thoại & phụ kiện  ");
		assertTrue(catOptCase.isPresent(),
				"Category JPA Optional checkNameExist phải tìm thấy khi viết thường và có khoảng trắng");

		// 2. Thêm mới trùng tên -> phải chặn
		com.fpoly.java5demo.entities.Category dupCat = new com.fpoly.java5demo.entities.Category();
		dupCat.setName("  Điện Thoại & Phụ Kiện  ");
		String addResult = categoryServices.addCategory(dupCat);
		assertEquals("Trùng tên", addResult, "Không cho phép thêm danh mục trùng tên");

		// 3. Thêm mới tên hợp lệ -> thành công
		com.fpoly.java5demo.entities.Category newCat = new com.fpoly.java5demo.entities.Category();
		newCat.setName("Đồ Gia Dụng Mới Nhất 2026");
		String addSuccess = categoryServices.addCategory(newCat);
		assertNull(addSuccess, "Thêm danh mục tên mới phải thành công");
		assertNotNull(newCat.getId());

		// 4. Sửa danh mục giữ nguyên tên cũ của chính nó -> thành công (id trùng id
		// đang sửa)
		String updateSelf = categoryServices.updateCategory(newCat);
		assertNull(updateSelf, "Sửa danh mục giữ nguyên tên chính nó phải thành công");

		// 5. Sửa danh mục thành tên danh mục khác đã có -> phải chặn
		newCat.setName("Điện Thoại & Phụ Kiện");
		String updateDup = categoryServices.updateCategory(newCat);
		assertEquals("Trùng tên", updateDup, "Không cho phép sửa danh mục trùng tên với danh mục khác");
	}

	@Test
	void testProductDuplicateValidation() {
		// 1. Kiểm tra tồn tại bằng Optional
		String existingName = "Điện thoại Apple iPhone 15 Pro Max 256GB Titan Tự Nhiên";
		java.util.Optional<com.fpoly.java5demo.entities.Product> prodOpt = productJPA.checkNameExist(existingName);
		assertTrue(prodOpt.isPresent(), "Product JPA Optional checkNameExist phải tìm thấy sản phẩm");

		// Kiểm tra không phân biệt hoa thường và khoảng trắng
		java.util.Optional<com.fpoly.java5demo.entities.Product> prodOptCase = productJPA
				.checkNameExist("  điện thoại apple iphone 15 pro max 256gb titan tự nhiên  ");
		assertTrue(prodOptCase.isPresent(),
				"Product JPA Optional checkNameExist phải tìm thấy khi viết thường và có khoảng trắng");

		// 2. Thêm mới sản phẩm trùng tên -> phải chặn
		com.fpoly.java5demo.entities.Product dupProd = new com.fpoly.java5demo.entities.Product();
		dupProd.setName("  " + existingName.toLowerCase() + "  ");
		dupProd.setPrice(1000000);
		dupProd.setQuantity(10);
		dupProd.setStatus(true);
		dupProd.setCategory(categoryServices.getList().get(0));
		String addResult = productServices.addProduct(dupProd, null);
		assertEquals("Trùng tên", addResult, "Không cho phép thêm sản phẩm trùng tên");

		// 3. Thêm mới sản phẩm tên hợp lệ -> thành công
		com.fpoly.java5demo.entities.Product newProd = new com.fpoly.java5demo.entities.Product();
		newProd.setName("Sản Phẩm Độc Nhất Vô Nhị 2026");
		newProd.setPrice(500000);
		newProd.setQuantity(20);
		newProd.setStatus(true);
		newProd.setCategory(categoryServices.getList().get(0));
		String addSuccess = productServices.addProduct(newProd, null);
		assertNull(addSuccess, "Thêm sản phẩm tên mới phải thành công");
		assertTrue(newProd.getId() > 0);

		// 4. Sửa sản phẩm giữ nguyên tên của chính nó -> thành công
		String updateSelf = productServices.updateProduct(newProd, null);
		assertNull(updateSelf, "Sửa sản phẩm giữ nguyên tên chính nó phải thành công");

		// 5. Sửa sản phẩm thành tên sản phẩm khác đã tồn tại -> phải chặn
		newProd.setName(existingName);
		String updateDup = productServices.updateProduct(newProd, null);
		assertEquals("Trùng tên", updateDup, "Không cho phép sửa sản phẩm trùng tên với sản phẩm khác");

		// Xác nhận trong CSDL tên sản phẩm không bị đổi sang tên trùng
		com.fpoly.java5demo.entities.Product prodInDb = productServices.findById(newProd.getId());
		assertEquals("Sản Phẩm Độc Nhất Vô Nhị 2026", prodInDb.getName(),
				"Tên trong CSDL phải được giữ nguyên khi update thất bại");
	}

	@Test
	void testFormBeanTrimming() {
		// CategoryFormBean trim test
		com.fpoly.java5demo.beans.CategoryFormBean catBean = new com.fpoly.java5demo.beans.CategoryFormBean();
		catBean.setName("   Thiết Bị Âm Thanh   ");
		assertEquals("Thiết Bị Âm Thanh", catBean.getName(), "CategoryFormBean.setName phải tự động trim 2 đầu");
		com.fpoly.java5demo.entities.Category cat = catBean.convertBeanToEntity();
		assertEquals("Thiết Bị Âm Thanh", cat.getName(), "convertBeanToEntity phải mang tên đã được trim");

		// ProductFormBean trim test
		com.fpoly.java5demo.beans.ProductFormBean prodBean = new com.fpoly.java5demo.beans.ProductFormBean();
		prodBean.setName("   Tai Nghe Sony WH-1000XM5   ");
		assertEquals("Tai Nghe Sony WH-1000XM5", prodBean.getName(), "ProductFormBean.setName phải tự động trim 2 đầu");
	}
}
