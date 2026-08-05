package com.example.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.user.dto.UserRegistrationDto;
import com.example.user.model.UpdatePasswordRequest;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.example.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Tests Unitaires")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("0600000000");
        user.setRole("ROLE_USER");
        user.setEnabled(true);

        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("rawPassword123");
        registrationDto.setConfirmPassword("rawPassword123");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setPhone("0600000000");
    }

    @Nested
    @DisplayName("Tests d'inscription (registerUser)")
    class RegisterTests {

        @Test
        @DisplayName("doit inscrire l'utilisateur avec succès")
        void shouldRegisterUserSuccessfully() {
            
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword123");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

           
            User result = userService.registerUser(registrationDto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(registrationDto.getEmail());
            assertThat(result.getPassword()).isEqualTo("encodedPassword123");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getPhone()).isEqualTo("0600000000");
            assertThat(result.getRole()).isEqualTo("ROLE_USER");
            assertThat(result.getEnabled()).isTrue();

            verify(userRepository).existsByEmail(registrationDto.getEmail());
            verify(passwordEncoder).encode(registrationDto.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("doit lever une exception si l'email existe déjà")
        void shouldThrowException_WhenEmailAlreadyExists() {
           
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");

            verify(userRepository).existsByEmail(registrationDto.getEmail());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit lever une exception si les mots de passe ne correspondent pas")
        void shouldThrowException_WhenRegisterPasswordsDoNotMatch() {
            
            registrationDto.setConfirmPassword("differentPassword");
            when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);

           
            assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Passwords do not match");

            verify(userRepository).existsByEmail(registrationDto.getEmail());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de changement de mot de passe (changePassword)")
    class ChangePasswordTests {

        private UpdatePasswordRequest passwordRequest;

        @BeforeEach
        void setUpPasswordRequest() {
            passwordRequest = new UpdatePasswordRequest();
            passwordRequest.setOldPassword("oldRawPassword");
            passwordRequest.setPassword("newRawPassword");
            passwordRequest.setConfirmPassword("newRawPassword");
        }

        @Test
        @DisplayName("doit changer le mot de passe avec succès")
        void shouldChangePasswordSuccessfully() {
           
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldRawPassword", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newRawPassword")).thenReturn("newEncodedPassword");

           
            userService.changePassword(user.getEmail(), passwordRequest);

            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");

            verify(userRepository).findByEmail(user.getEmail());
            verify(passwordEncoder).matches("oldRawPassword", "encodedPassword123");
            verify(passwordEncoder).encode("newRawPassword");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("doit lever une exception si l'utilisateur n'est pas trouvé")
        void shouldThrowException_WhenUserNotFound() {
          
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword("unknown@example.com", passwordRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

            verify(userRepository).findByEmail("unknown@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit lever une exception si l'ancien mot de passe est invalide")
        void shouldThrowException_WhenOldPasswordIsInvalid() {
           
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldRawPassword", user.getPassword())).thenReturn(false);

           
            assertThatThrownBy(() -> userService.changePassword(user.getEmail(), passwordRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid old password");

            verify(userRepository).findByEmail(user.getEmail());
            verify(passwordEncoder).matches("oldRawPassword", user.getPassword());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit lever une exception si le nouveau mot de passe et sa confirmation ne correspondent pas")
        void shouldThrowException_WhenNewPasswordsDoNotMatch() {
          
            passwordRequest.setConfirmPassword("mismatchedPassword");

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldRawPassword", user.getPassword())).thenReturn(true);

            assertThatThrownBy(() -> userService.changePassword(user.getEmail(), passwordRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Passwords do not match");

            verify(userRepository).findByEmail(user.getEmail());
            verify(passwordEncoder).matches("oldRawPassword", user.getPassword());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests Spring Security (loadUserByUsername)")
    class UserDetailsServiceTests {

        @Test
        @DisplayName("loadUserByUsername - doit charger les détails de l'utilisateur")
        void shouldLoadUserByUsernameSuccessfully() {
           
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
            assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
            assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

            verify(userRepository).findByEmail(user.getEmail());
        }

        @Test
        @DisplayName("loadUserByUsername - doit lever UsernameNotFoundException si utilisateur inconnu")
        void shouldThrowUsernameNotFoundException_WhenUserNotFound() {
           
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

           
            assertThatThrownBy(() -> userService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

            verify(userRepository).findByEmail("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("Tests CRUD de base")
    class CrudTests {

        @Test
        @DisplayName("findByEmail - doit retourner l'utilisateur si trouvé")
        void shouldFindByEmail() {
            
            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

          
            Optional<User> result = userService.findByEmail(user.getEmail());

            assertThat(result).isPresent().contains(user);
            verify(userRepository).findByEmail(user.getEmail());
        }

        @Test
        @DisplayName("findById - doit retourner l'utilisateur si trouvé")
        void shouldFindById() {
          
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

          
            Optional<User> result = userService.findById(1L);

            assertThat(result).isPresent().contains(user);
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("save - doit sauvegarder et retourner l'utilisateur")
        void shouldSaveUser() {
           
            when(userRepository.save(user)).thenReturn(user);

            
            User result = userService.save(user);

           
            assertThat(result).isNotNull().isEqualTo(user);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("findAll - doit retourner la liste de tous les utilisateurs")
        void shouldFindAllUsers() {
          
            when(userRepository.findAll()).thenReturn(List.of(user));

           
            List<User> result = userService.findAll();

           
            assertThat(result).isNotNull().hasSize(1).containsExactly(user);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("deleteById - doit appeler deleteById sur le repository")
        void shouldDeleteUserById() {
           
            userService.deleteById(1L);

           
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("updateUser - doit mettre à jour et retourner l'utilisateur")
        void shouldUpdateUser() {
           
            when(userRepository.save(user)).thenReturn(user);

          
            User result = userService.updateUser(user);

            assertThat(result).isNotNull().isEqualTo(user);
            verify(userRepository).save(user);
        }
    }
}