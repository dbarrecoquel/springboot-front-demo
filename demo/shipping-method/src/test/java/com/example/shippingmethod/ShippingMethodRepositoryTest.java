package com.example.shippingmethod;

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
import org.springframework.data.repository.query.Param;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.shippingmethod.model.ShippingMethod;
import com.example.shippingmethod.repository.ShippingMethodRepository;


@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("ShippingMethodRepository — Integration Tests")
public class ShippingMethodRepositoryTest {
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
	@Autowired ShippingMethodRepository shippingMethodRepository;
	private ShippingMethod shippingMethod;
	private ShippingMethod shippingMethod2;
	@BeforeEach()
	void setUp() {
		
		shippingMethod = new ShippingMethod();
		
		shippingMethod.setName("DHL");
		shippingMethod.setCost(50D);
		shippingMethod.setEnabled(true);
		shippingMethod.setEstimatedDays(5);
		shippingMethod.setDestinationCountry("EU");
	
		em.persistAndFlush(shippingMethod);
		
		shippingMethod2 = new ShippingMethod();
		
		shippingMethod2.setName("LA_POSTE");
		shippingMethod2.setCost(30D);
		shippingMethod2.setEnabled(false);
		shippingMethod2.setEstimatedDays(5);
		shippingMethod2.setDestinationCountry("EU");
		em.persistAndFlush(shippingMethod2);
		
		em.clear();
		
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("findByName - found")
		void findByName_found() {
			
			Optional<ShippingMethod> shippingMethod = shippingMethodRepository.findByName("DHL");
			assertThat(shippingMethod.isPresent());
			assertThat(shippingMethod.get().getCost()).isEqualTo(50D);
			
		}
		@Test
		@DisplayName("findByName - notfound")
		void findByName_notfound() {
			
			Optional<ShippingMethod> shippingMethod = shippingMethodRepository.findByName("test");
			assertThat(shippingMethod.isEmpty());
			
		}

		@Test
		@DisplayName("findByEnabledTrue - notfound")
		void findByEnabledTrue() {
			
			List<ShippingMethod> shippingMethod = shippingMethodRepository.findByEnabledTrue();
			assertThat(shippingMethod).hasSize(1);
		}
		@Test
		@DisplayName("findByDestinationCountry - found")
		void findByDestinationCountry_found() {
			
			List<ShippingMethod> shippingMethod = shippingMethodRepository.findByDestinationCountry("EU");
			assertThat(shippingMethod).hasSize(2);
		}
		@Test
		@DisplayName("findByDestinationCountry - notfound")
		void findByDestinationCountry_notfound() {
			
			List<ShippingMethod> shippingMethod = shippingMethodRepository.findByDestinationCountry("Test");
			assertThat(shippingMethod).isEmpty();
		}
		@Test
		@DisplayName("findAvailableForCountry - found")
		void findAvailableForCountry_found() {
			
			List<ShippingMethod> shippingMethod = shippingMethodRepository.findAvailableForCountry("France");
			assertThat(shippingMethod).hasSize(1);
		}
		@Test
		@DisplayName("findAvailableForCountry - notfound")
		void findAvailableForCountry_notfound() {
			
			List<ShippingMethod> shippingMethod = shippingMethodRepository.findAvailableForCountry("Test");
			assertThat(shippingMethod).isEmpty();
		}
		@Test
		@DisplayName("findAllDestinationCountries")
		void findAllDestinationCountries() {
			List<String> l = shippingMethodRepository.findAllDestinationCountries();
			assertThat(l.contains("EU"));
			assertThat(l).hasSize(1);
		}
	}
}
