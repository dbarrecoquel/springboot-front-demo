package com.example.address;

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

import com.example.address.model.Address;
import com.example.address.repository.AddressRepository;
import com.example.user.model.User;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = TestApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("AddressRepository — Integration Tests")
public class AddressRepositoryTest {
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
	
	@Autowired AddressRepository addressRepository;
	@Autowired TestEntityManager em;
	private Address address;
	private User user;
	@BeforeEach()
	void setUp() {
		user = new User();
		user.setEmail("test@test.fr");
		user.setFirstName("test");
		user.setLastName("test");
		user.setPassword("Password");
		em.persistAndFlush(user);
		
		address = new Address();
		address.setAddressType("SHIPPING");
		address.setCity("Douai");
		address.setCountry("France");
		address.setLabel("Address");
		address.setIsDefault(true);
		address.setStreet("1 rue test");
		address.setUserId(user.getId());
		address.setPostalCode("59500");
		address.setUser(user);
		
		em.persistAndFlush(address);
		em.clear();
	}
	
	@Nested
	@DisplayName("Recherches simple")
	class FindTests {
		
		@Test
		@DisplayName("findByUserId - found")
		void findByUserId_found() {
			
			List<Address> addresses = addressRepository.findByUserId(user.getId());
			assertThat(addresses).hasSize(1);
			assertThat(addresses.get(0).getLabel()).isEqualTo("Address");
			
		}
		
		@Test
		@DisplayName("findByUserId - not found")
		void findByUserId_notfound() {
			
			List<Address> addresses = addressRepository.findByUserId(user.getId() + 999L);
			assertThat(addresses).isEmpty();
			
		}
		
		@Test
		@DisplayName("findByUserIdAndAddressType - found ")
		void findByUserIdAndAddressType_found() {
			
			List<Address> addresses = addressRepository.findByUserIdAndAddressType(user.getId(), "SHIPPING");
			assertThat(addresses).hasSize(1);
			assertThat(addresses.get(0).getLabel()).isEqualTo("Address");

		}
		@Test
		@DisplayName("findByUserIdAndAddressType - notfound ")
		void findByUserIdAndAddressType_notfound() {
			
			List<Address> addresses = addressRepository.findByUserIdAndAddressType(user.getId(), "BILLING");
			assertThat(addresses).isEmpty();

		}
		@Test
		@DisplayName("findByUserIdAndIsDefaultTrue - found ")
		void findByUserIdAndIsDefaultTrue_found() {
			
			Address addr = addressRepository.findByUserIdAndIsDefaultTrue(user.getId());
			assertThat(addr).isNotNull();
			assertThat(addr.getLabel()).isEqualTo("Address");
			
		}
		@Test
		@DisplayName("findByUserIdAndIsDefaultTrue - notfound ")
		void findByUserIdAndIsDefaultTrue_notfound() {
	
			Address addr = addressRepository.findByUserIdAndIsDefaultTrue(user.getId() + 999L);
			assertThat(addr).isNull();

		}
		
	}
}
