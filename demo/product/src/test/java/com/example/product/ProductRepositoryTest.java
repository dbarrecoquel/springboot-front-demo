package com.example.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;


@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("ProductRepository — Integration Tests")
public class ProductRepositoryTest {
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
	@Autowired ProductRepository productRepository;
	
	private Product product;
	
	@BeforeEach()
	void setUp() {
		
		product = new Product();
		product.setName("Name");
		product.setSku("Sku");
		product.setPrice(100D);
		
		em.persistAndFlush(product);
		
		em.clear();
		
	}
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("findBySku - found")
		void findBySku_found() {
			
			Optional<Product> p = productRepository.findBySku("Sku");
			assertThat(p.isPresent());
			assertThat(p.get().getName()).isEqualTo("Name");
			
		}
		@Test
		@DisplayName("findBySku - notfound")
		void findBySku_notfound() {
			
			Optional<Product> p = productRepository.findBySku("test");
			assertThat(p.isEmpty());
			
		}
		@Test
		@DisplayName("findWithFilters - found")
		void findWithFilters_found() {
			
			Pageable pageable = PageRequest.of(0, 10);
			Page<Product> page = productRepository.findWithFilters("Name", 50D, 150D, pageable);
			assertThat(page).hasSize(1);
			assertThat(page.getContent().get(0).getName()).isEqualTo("Name");
			
		}
		@Test
		@DisplayName("findWithFilters - notfound")
		void findWithFilters_notfound() {
			
			Pageable pageable = PageRequest.of(0, 10);
			Page<Product> page = productRepository.findWithFilters("Name2", 50D, 150D, pageable);
			assertThat(page).isEmpty();
			
		}
	}
}
