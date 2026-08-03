package com.example.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.address.model.Address;
import com.example.address.repository.AddressRepository;
import com.example.address.service.AddressService;
import com.example.user.model.User;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
	
	@Mock
	private AddressRepository addressRepository;
	
	@InjectMocks
	private AddressService addressService;
	
	private Address address;
	private Address address2;
	private User user;
	
	@BeforeEach
	void setUp() {
		user = new User();
		user.setEmail("test@test.fr");
		user.setFirstName("test");
		user.setLastName("test");
		user.setPassword("Password");
		
		address = new Address();
		address.setId(1L);
		address.setAddressType("SHIPPING");
		address.setCity("Douai");
		address.setCountry("France");
		address.setLabel("Address");
		address.setIsDefault(true);
		address.setStreet("1 rue test");
		address.setUserId(user.getId());
		address.setPostalCode("59500");
		address.setUser(user);
		
		address2 = new Address();
		address2.setId(2L);
		address2.setAddressType("SHIPPING");
		address2.setCity("Douai");
		address2.setCountry("France");
		address2.setLabel("Address");
		address2.setIsDefault(true);
		address2.setStreet("1 rue test");
		address2.setUserId(user.getId());
		address2.setPostalCode("59500");
		address2.setUser(user);
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests{
		
	
		@Test
		@DisplayName("doit retrouvé l'adresse par userId")
		void shouldgetAddressesByUserId() {
			
			List<Address> addresses = List.of(address);
			when(addressRepository.findByUserId(user.getId())).thenReturn(addresses);
			
			List<Address> result = addressService.getAddressesByUserId(user.getId());
			assertNotNull(result);
			assertEquals(1, result.size());
			
			verify(addressRepository).findByUserId(user.getId());
		}
		@Test
		@DisplayName("doit retrouvé les adresses par userId et Type")
		void shouldgetAddressesByUserIdAndType() {
			List<Address> addresses = List.of(address);
			when(addressRepository.findByUserIdAndAddressType(user.getId(),"SHIPPING")).thenReturn(addresses);
			
			List<Address> result = addressService.getAddressesByUserIdAndType(user.getId(),"SHIPPING");
			assertNotNull(result);
			assertEquals(1, result.size());
			
			verify(addressRepository).findByUserIdAndAddressType(user.getId(), "SHIPPING");
			
		}
		@Test
		@DisplayName("doit retrouvé l'adresse par l'id")
		void shouldGetAddressById() {
			
			when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
			
			Optional<Address> addr = addressService.getAddressById(address.getId());
			
			assertThat(addr).isPresent();
			assertEquals(addr.get().getAddressType(),"SHIPPING");
			
			verify(addressRepository).findById(address.getId());
		}
		@Test
		@DisplayName("getAddressById - non trouvé doit retourner Optional.empty()")
		void shouldReturnEmptyOptional_WhenAddressNotFound() {
		    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

		    Optional<Address> result = addressService.getAddressById(99L);

		    assertThat(result).isEmpty();
		    verify(addressRepository).findById(99L);
		}
		@Test
		@DisplayName("doit retrouvé l'adresse par défaut")
		void shouldGetDefaultAddress() {
			when(addressRepository.findByUserIdAndIsDefaultTrue(user.getId())).thenReturn(address);
			
			Address result = addressService.getDefaultAddress(user.getId());
			assertNotNull(result);
			
			verify(addressRepository).findByUserIdAndIsDefaultTrue(user.getId());
		}
		

		@Test
		@DisplayName("getDefaultAddress - non trouvé doit retourner null")
		void shouldReturnNull_WhenNoDefaultAddressExists() {
		    when(addressRepository.findByUserIdAndIsDefaultTrue(user.getId())).thenReturn(null);

		    Address result = addressService.getDefaultAddress(user.getId());

		    assertThat(result).isNull();
		    verify(addressRepository).findByUserIdAndIsDefaultTrue(user.getId());
		}
	}
	@Nested
	@DisplayName("Tests de saveAddress")
	class SaveAddressTests {

		@Test
		@DisplayName("Doit réinitialiser l'attribut isDefault des autres adresses si la nouvelle est par défaut")
		void shouldResetOtherDefaultAddresses_WhenNewAddressIsDefault() {
			List<Address> existingAddresses = List.of(address, address2);
			when(addressRepository.findByUserId(user.getId())).thenReturn(existingAddresses);
			when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

			Address savedAddress = addressService.saveAddress(address);

			assertThat(address2.getIsDefault()).isFalse();
			verify(addressRepository).save(address2);
			assertThat(savedAddress).isNotNull();
			assertThat(savedAddress.getIsDefault()).isTrue();
			verify(addressRepository).save(address);
		}

		@Test
		@DisplayName("Ne doit pas rechercher ni modifier les autres adresses si isDefault est false")
		void shouldNotModifyOtherAddresses_WhenAddressIsNotDefault() {
			
			address.setIsDefault(false);
			when(addressRepository.save(address)).thenReturn(address);

			Address savedAddress = addressService.saveAddress(address);

			// THEN
			assertThat(savedAddress).isNotNull();
			assertThat(savedAddress.getIsDefault()).isFalse();

			verify(addressRepository, never()).findByUserId(any());
			verify(addressRepository).save(address);
		}
		@Test
		@DisplayName("Doit réinitialiser les autres adresses lors de la CRÉATION d'une nouvelle adresse par défaut (id null)")
		void shouldResetOtherDefaultAddresses_WhenCreatingNewAddressWithNullId() {
		    Address newAddress = new Address();
		    newAddress.setId(null); 
		    newAddress.setUserId(user.getId());
		    newAddress.setIsDefault(true);

		    List<Address> existingAddresses = List.of(address2); 
		    when(addressRepository.findByUserId(user.getId())).thenReturn(existingAddresses);
		    when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

		    Address saved = addressService.saveAddress(newAddress);

		    assertThat(address2.getIsDefault()).isFalse();
		    verify(addressRepository).save(address2);
		    verify(addressRepository).save(newAddress);
		}
	}
	@Nested
	@DisplayName("Remove tests")
	class RemoveTests{
		
		@Test
		@DisplayName("should remove address")
		void shouldRemoveAddress() {
			addressService.deleteAddress(1L);
			
			verify(addressRepository).deleteById(1L);
		}
		
	}
}