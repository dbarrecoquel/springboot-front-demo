package com.example.shopping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.product.model.Product;
import com.example.shopping.model.Basket;
import com.example.shopping.model.ProductLineItem;
import com.example.shopping.repository.ProductLineItemRepository;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("ProductLineItemRepository — Integration Tests")
public class ProductLineItemRepositoryTest {
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
	    .withDatabaseName("ecommerce_test")
	    .withUsername("testuser")
	    .withPassword("testpass")
	    .withReuse(true);
	
	@DynamicPropertySource
	static void overideDataSource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.hikari.auto-commit", () -> "false");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
	}
	
	@Autowired TestEntityManager em;
	@Autowired ProductLineItemRepository productLineItemRepository;
	
	private Basket basket;
	private ProductLineItem productLineItem;
	private Product product;
	@BeforeEach
	void setUp() {
		
		basket = new Basket();
		basket.setGuestId(UUID.randomUUID().toString());
		basket.setUserId(1L);
		basket.setSessionId(UUID.randomUUID().toString());
		
		em.persistAndFlush(basket);
		
		product = new Product();
		product.setName("Name");
		product.setSku("Test");
		product.setPrice(50D);
		
		em.persistAndFlush(product);
		
		productLineItem = new ProductLineItem();
		productLineItem.setBasketId(basket.getId());
		productLineItem.setProduct(product);
		productLineItem.setProductId(product.getId());
		productLineItem.setQuantity(2);
		productLineItem.setUnitPrice(50D);
		
		em.persistAndFlush(productLineItem);
		
		em.clear();
		
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("findByBasketId - found")
		void findByBasketId_found() {
			List<ProductLineItem> plis = productLineItemRepository.findByBasketId(basket.getId());
			assertThat(plis).isNotEmpty();
			assertThat(plis).hasSize(1);
		}
		@Test
		@DisplayName("findByBasketId - notfound")
		void findByBasketId_notfound() {
			List<ProductLineItem> plis = productLineItemRepository.findByBasketId(basket.getId()+999L);
			assertThat(plis).isEmpty();
		}
		@Test
		@DisplayName("findByBasketIdAndProductId - found")
		void findByBasketIdAndProductId_found() {
			Optional<ProductLineItem> plis = productLineItemRepository.findByBasketIdAndProductId(basket.getId(),product.getId());
			assertThat(plis).isNotEmpty();
			assertThat(plis).isPresent();
		}
		@Test
		@DisplayName("findByBasketIdAndProductId - notfound")
		void findByBasketIdAndProductId_notfound() {
			Optional<ProductLineItem> plis = productLineItemRepository.findByBasketIdAndProductId(basket.getId()+999L,product.getId()+999L);
			assertThat(plis).isEmpty();
		}
	}
}
