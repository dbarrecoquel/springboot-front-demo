package com.example.shopping;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.example.shopping.model.Basket;
import com.example.shopping.repository.BasketRepository;


@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("BasketRepository — Integration Tests")
public class BasketRepositoryTest {
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
	@Autowired BasketRepository basketRepository;
	
	private Basket basket;
	
	@BeforeEach
	void setup() {
		
		basket = new Basket();
		basket.setGuestId(UUID.randomUUID().toString());
		basket.setSessionId(UUID.randomUUID().toString());
		basket.setUserId(1L);
		
		em.persistAndFlush(basket);
		em.clear();
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests{
		
		@Test
		@DisplayName("findByUserId - found")
		void findByUserId_found() {
			
			Optional<Basket> b = basketRepository.findByUserId(1L);
			assertThat(b).isPresent();
		}
		@Test
		@DisplayName("findByUserId - notfound")
		void findByUserId_notfound() {
			
			Optional<Basket> b = basketRepository.findByUserId(2L);
			assertThat(b).isEmpty();
		}
		@Test
		@DisplayName("findBySessionId - found")
		void findBySessionId_found() {
			
			Optional<Basket> b = basketRepository.findBySessionId(basket.getSessionId());
			assertThat(b).isPresent();
		}
		@Test
		@DisplayName("findBySessionId - notfound")
		void findBySessionId_notfound() {
			
			Optional<Basket> b = basketRepository.findBySessionId(UUID.randomUUID().toString());
			assertThat(b).isEmpty();
		}
		@Test
		@DisplayName("findByGuestId - found")
		void findByGuestId_found() {
			
			Optional<Basket> b = basketRepository.findByGuestId(basket.getGuestId());
			assertThat(b).isPresent();
		}
		@Test
		@DisplayName("findByGuestId - notfound")
		void findByGuestId_notfound() {
			
			Optional<Basket> b = basketRepository.findByGuestId(UUID.randomUUID().toString());
			assertThat(b).isEmpty();
		}
	}
}
