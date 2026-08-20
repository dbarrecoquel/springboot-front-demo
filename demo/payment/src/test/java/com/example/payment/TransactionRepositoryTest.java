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

import com.example.payment.enums.PaymentMethodType;
import com.example.payment.enums.TransactionStatus;
import com.example.payment.model.PaymentMethod;
import com.example.payment.model.Transaction;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.TransactionRepository;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("PaymentMethodRepository — Integration Tests")
public class TransactionRepositoryTest {
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
	@Autowired TransactionRepository transactionRepository;
	@Autowired TestEntityManager em;
	
	private PaymentMethod paymentMethod;
	private Transaction transaction;
	
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
		
		transaction = new Transaction();
		transaction.setAmount(100D);
		transaction.setErrorMessage(null);
		transaction.setExternalReference(null);
		transaction.setTransactionNumber("TXN-1");
		transaction.setNotes(null);
		transaction.setOrderId(1L);
		transaction.setPaymentMethod(paymentMethod);
		transaction.setStatus(TransactionStatus.PENDING);
		em.persistAndFlush(transaction);
		
		
		em.clear();
		
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests{
		
		@Test
		@DisplayName("findByTransactionNumber - found")
		void findByTransactionNumber_found() {
			
			Optional<Transaction> transaction = transactionRepository.findByTransactionNumber("TXN-1");
			
			assertThat(transaction).isNotEmpty();
			assertThat(transaction.get().getAmount()).isEqualTo(100D);
		}
		@Test
		@DisplayName("findByTransactionNumber - notfound")
		void findByTransactionNumber_notfound() {
			
			Optional<Transaction> transaction = transactionRepository.findByTransactionNumber("TXN-2");
			
			assertThat(transaction).isEmpty();
		}
		@Test
		@DisplayName("findByOrderId - found")
		void findByOrderId_found() {
			
			Optional<Transaction> transaction = transactionRepository.findByOrderId(1L);
			
			assertThat(transaction).isNotEmpty();
			assertThat(transaction.get().getTransactionNumber()).isEqualTo("TXN-1");
			assertThat(transaction.get().getAmount()).isEqualTo(100D);
		}
		@Test
		@DisplayName("findByOrderId - notfound")
		void findByOrderId_notfound() {
			
			Optional<Transaction> transaction = transactionRepository.findByOrderId(2L);
			
			assertThat(transaction).isEmpty();
		}
		@Test
		@DisplayName("findByStatus - found")
		void findByStatus_found() {
			
			List<Transaction> transaction = transactionRepository.findByStatus(TransactionStatus.PENDING);
			
			assertThat(transaction).isNotEmpty();
			assertThat(transaction.get(0).getTransactionNumber()).isEqualTo("TXN-1");
			assertThat(transaction.get(0).getAmount()).isEqualTo(100D);
		}
		@Test
		@DisplayName("findByStatus - notfound")
		void findByStatus_notfound() {
			
			List<Transaction> transaction = transactionRepository.findByStatus(TransactionStatus.COMPLETED);
			
			assertThat(transaction).isEmpty();
		}
		
		@Test
		@DisplayName("findByStatusOrderByCreatedAtDesc - found")
		void findByStatusOrderByCreatedAtDesc_found() {
			
			Pageable pageable = PageRequest.of(0, 10);
			Page<Transaction> transactions = transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING, pageable);
			
			assertThat(transactions).isNotNull();
			assertThat(transactions.getContent()).hasSize(1);
			
		}
		@Test
		@DisplayName("findByStatusOrderByCreatedAtDesc - notfound")
		void findByStatusOrderByCreatedAtDesc_notfound() {
			
			Pageable pageable = PageRequest.of(0, 10);
			Page<Transaction> transactions = transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.COMPLETED, pageable);
			
			assertThat(transactions.getContent()).isEmpty();
			
		}
		@Test
		@DisplayName("findAllByOrderByCreatedAtDesc - found")
		void findAllByOrderByCreatedAtDesc_found() {
			
			Pageable pageable = PageRequest.of(0,10);
			Page<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
			
			assertThat(transactions).isNotNull();
			assertThat(transactions.getContent()).hasSize(1);
		}
	}
}
