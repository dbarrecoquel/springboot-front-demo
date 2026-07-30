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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.catalog.model.Category;
import com.example.catalog.repository.CategoryRepository;


@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("CategoryRepository — Integration Tests")
public class CategoryRepositoryTest {
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
	
	@Autowired CategoryRepository categoryRepository;
	@Autowired TestEntityManager em;
	private Category category;
	private Category parentCategory;
	
	@BeforeEach()
	void setUp() {
		
		
		parentCategory = new Category();
		parentCategory.setName("PARENT");
		
		em.persistAndFlush(parentCategory);
		
		category = new Category();
		category.setName("TEST");
		category.setParentCategory(parentCategory);
		category.setParentCategoryId(parentCategory.getId());
		
		em.persistAndFlush(category);
		
		em.clear();
		
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("findByParentCategoryIdIsNull - found")
		void findByParentCategoryIdIsNull_found() {
			
			List<Category> categories = categoryRepository.findByParentCategoryIdIsNull();
			assertThat(categories).hasSize(1);
			assertThat(categories.get(0).getName()).isEqualTo("PARENT");

		}
		@Test
		@DisplayName("findByParentCategoryId - found")
		void findByParentCategoryId_found() {
			
			List<Category> categories = categoryRepository.findByParentCategoryId(parentCategory.getId());
			assertThat(categories).hasSize(1);
			assertThat(categories.get(0).getName()).isEqualTo("TEST");

		}
		@Test
		@DisplayName("findByParentCategoryId - notfound")
		void findByParentCategoryId_notfound() {
			
			List<Category> categories = categoryRepository.findByParentCategoryId(parentCategory.getId()+999L);
			assertThat(categories).isEmpty();

		}
		
	}
	
}
