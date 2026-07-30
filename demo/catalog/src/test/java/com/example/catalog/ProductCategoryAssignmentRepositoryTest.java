package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

import com.example.catalog.model.Category;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.repository.ProductCategoryAssignmentRepository;
import com.example.product.model.Product;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("ProductCategoryAssignmentRepository — Integration Tests")
public class ProductCategoryAssignmentRepositoryTest {

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
	@Autowired ProductCategoryAssignmentRepository pcar;
	
	private Category category;
	private Product product;
	private ProductCategoryAssignment ass;
	@BeforeEach()
	void setUp() {
	
		category = new Category();
		category.setName("Category");
		em.persistAndFlush(category);
		
		product = new Product();
		product.setName("Test");
		product.setSku("sku");
		product.setPrice(100D);
		
		em.persistAndFlush(product);
		
		ass = new ProductCategoryAssignment();
		ass.setCategory(category);
		ass.setCategoryId(category.getId());
		ass.setProduct(product);
		ass.setProductId(product.getId());
		
		em.persistAndFlush(ass);
		em.clear();
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("findByProductId - found")
		void findByProductId_found() {
			
			List<ProductCategoryAssignment>	asgnmt = pcar.findByProductId(product.getId());
			assertThat(asgnmt).hasSize(1);
			assertThat(asgnmt.get(0).getCategory().getName()).isEqualTo("Category");
			assertThat(asgnmt.get(0).getProduct().getName()).isEqualTo("Test");
			
		}
		@Test
		@DisplayName("findByProductId - notfound")
		void findByProductId_notfound() {
			
			List<ProductCategoryAssignment>	asgnmt = pcar.findByProductId(product.getId()+999L);
			assertThat(asgnmt).isEmpty();
			
		}
		@Test
		@DisplayName("findByCategoryId - found")
		void findByCategoryId_found() {

			Pageable pageable = PageRequest.of(0, 10);
			Page<ProductCategoryAssignment>	asgnmt = pcar.findByCategoryId(category.getId(),pageable);
			assertThat(asgnmt.getTotalElements()).isEqualTo(1);
			assertThat(asgnmt.getContent().get(0).getCategory().getName()).isEqualTo("Category");
			assertThat(asgnmt.getContent().get(0).getProduct().getName()).isEqualTo("Test");
			
		}
		@Test
		@DisplayName("findByCategoryId - notfound")
		void findByCategoryId_notfound() {
			
			Pageable pageable = PageRequest.of(0, 10);
			Page<ProductCategoryAssignment>	asgnmt = pcar.findByCategoryId(category.getId()+999L,pageable);
			assertThat(asgnmt).isEmpty();
			
		}
	}
	
	@Nested
	@DisplayName("ExistBy recherches")
	class ExistByTests {
		
		@Test
		@DisplayName("existsByProductIdAndCategoryId - found")
		void existsByProductIdAndCategoryId_found() {
			
			boolean exist = pcar.existsByProductIdAndCategoryId(product.getId(), category.getId());
			assertThat(exist).isTrue();
			
		}
		@Test
		@DisplayName("existsByProductIdAndCategoryId - notfound")
		void existsByProductIdAndCategoryId_notfound() {
			
			boolean exist = pcar.existsByProductIdAndCategoryId(product.getId()+999L, category.getId());
			assertThat(exist).isFalse();
			
		}
	}
}
