package com.example.user;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;


@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("UserRepository — Integration Tests")
public class UserRepositoryTest {
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
	@Autowired UserRepository userRepository;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		
		user = new User();
		user.setEmail("test@test.fr");
		user.setEnabled(true);
		user.setFirstName("test");
		user.setLastName("test");
		user.setPassword("testpassword");
		user.setPhone("0123456789");
		user.setRole("ROLE_USER");
		
		em.persistAndFlush(user);
		em.clear();
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests{
		
		@Test
		@DisplayName("findByEmail - found")
		void findByEmail_found() {
			
			Optional<User> u = userRepository.findByEmail("test@test.fr");
			assertThat(u).isPresent();
			assertThat(u.get().getFirstName()).isEqualTo("test");
		}
		@Test
		@DisplayName("findByEmail - notfound")
		void findByEmail_notfound() {
			
			Optional<User> u = userRepository.findByEmail("tes2@test.fr");
			assertThat(u).isEmpty();
			
		}
		@Test
		@DisplayName("existsByEmail - found")
		void existsByEmail_found() {
			
			boolean u = userRepository.existsByEmail("test@test.fr");
			assertThat(u).isTrue();
		}
		@Test
		@DisplayName("existsByEmail - notfound")
		void existsByEmail_notfound() {
			
			boolean u = userRepository.existsByEmail("tes2@test.fr");
			assertThat(u).isFalse();
			
		}
		
	}
}
