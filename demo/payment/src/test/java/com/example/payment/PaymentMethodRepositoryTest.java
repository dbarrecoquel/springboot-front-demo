package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

import com.example.payment.enums.PaymentMethodType;
import com.example.payment.model.PaymentMethod;
import com.example.payment.repository.PaymentMethodRepository;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("PaymentMethodRepository — Integration Tests")
public class PaymentMethodRepositoryTest {
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
	static {
	    postgres.start(); // Guarantees container starts BEFORE Spring Context initialization
	}
	@Autowired PaymentMethodRepository paymentMethodRepository;
	@Autowired TestEntityManager em;
	
	private PaymentMethod paymentMethod;
	
	@BeforeEach
	void setUp() {
		
		paymentMethod = new PaymentMethod();
		//paymentMethod.setId(1L);
		paymentMethod.setName("COD");
		paymentMethod.setDescription("Paiement à la livraison");
		paymentMethod.setType(PaymentMethodType.COD);
		paymentMethod.setDisplayOrder(0);
		paymentMethod.setEnabled(true);
		em.persistAndFlush(paymentMethod);
		em.clear();
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests{
		
		@Test
		@DisplayName("findByType - found")
		void findByType_found() {
			
			Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findByType(PaymentMethodType.COD);
			
			assertThat(paymentMethod).isNotEmpty();
			assertThat(paymentMethod.get().getType()).isEqualTo(PaymentMethodType.COD);
			assertThat(paymentMethod.get().getName()).isEqualTo("COD");
		}
		@Test
		@DisplayName("findByType - not found")
		void findByType_notfound() {
			
			Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findByType(PaymentMethodType.CREDIT_CARD);
			
			assertThat(paymentMethod).isEmpty();
		}
		
		@Test
		@DisplayName("findByEnabledOrderByDisplayOrder - found") 
		void findByEnabledOrderByDisplayOrder_found() {	
			List<PaymentMethod> paymentMethods = paymentMethodRepository.findByEnabledOrderByDisplayOrder(true);
			
			assertThat(paymentMethods).isNotEmpty();
			assertThat(paymentMethods).hasSize(1);
		}
		@Test
		@DisplayName("findByEnabledOrderByDisplayOrder - notfound") 
		void findByEnabledOrderByDisplayOrder_notfound() {	
			List<PaymentMethod> paymentMethods = paymentMethodRepository.findByEnabledOrderByDisplayOrder(false);
			
			assertThat(paymentMethods).isEmpty();
		}
		
		@Test
		@DisplayName("findAllByOrderByDisplayOrder - found")
		void findAllByOrderByDisplayOrder_found() {
			
			List<PaymentMethod> paymentMethods = paymentMethodRepository.findAllByOrderByDisplayOrder();
			
			assertThat(paymentMethods).isNotEmpty();
			assertThat(paymentMethods).hasSize(1);
		}
	}
}
