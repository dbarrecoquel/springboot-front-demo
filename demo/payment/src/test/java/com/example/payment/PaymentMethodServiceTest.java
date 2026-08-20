package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
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

import com.example.payment.dto.PaymentMethodDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.mapper.PaymentMethodMapper;
import com.example.payment.model.PaymentMethod;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.service.PaymentMethodService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMethodService — Tests Unitaires")
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentMethodMapper paymentMethodMapper;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    private PaymentMethod entity;
    private PaymentMethodDto dto;

    @BeforeEach
    void setUp() {
        entity = new PaymentMethod();
        entity.setId(1L);
        entity.setType(PaymentMethodType.CREDIT_CARD); // Remplace par une valeur de ton enum
        entity.setName("Carte bancaire");
        entity.setEnabled(true);
        entity.setDisplayOrder(1);

        dto = new PaymentMethodDto();
        dto.setId(1L);
        dto.setType(PaymentMethodType.CREDIT_CARD);
        dto.setName("Carte bancaire");
        dto.setEnabled(true);
        dto.setDisplayOrder(1);
    }

    @Nested
    @DisplayName("Recherches simples")
    class FindTests {

        @Test
        @DisplayName("devrait retourner le DTO quand l'ID existe")
        void shouldReturnDtoWhenIdExists() {
            when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(paymentMethodMapper.toDto(entity)).thenReturn(dto);

            Optional<PaymentMethodDto> result = paymentMethodService.getPaymentMethodById(1L);

            assertThat(result).isPresent().contains(dto);
            verify(paymentMethodRepository).findById(1L);
            verify(paymentMethodMapper).toDto(entity);
        }

        @Test
        @DisplayName("devrait retourner Optional.empty() quand l'ID n'existe pas")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<PaymentMethodDto> result = paymentMethodService.getPaymentMethodById(99L);

            assertThat(result).isEmpty();
            verify(paymentMethodRepository).findById(99L);
            verify(paymentMethodMapper, never()).toDto(any());
        }
    

        @Test
        @DisplayName("devrait retourner le DTO quand le Type existe")
        void shouldReturnDtoWhenTypeExists() {
            PaymentMethodType type = PaymentMethodType.CREDIT_CARD;
            when(paymentMethodRepository.findByType(type)).thenReturn(Optional.of(entity));
            when(paymentMethodMapper.toDto(entity)).thenReturn(dto);

            Optional<PaymentMethodDto> result = paymentMethodService.getPaymentMethodByType(type);

            assertThat(result).isPresent().contains(dto);
            verify(paymentMethodRepository).findByType(type);
        }

        @Test
        @DisplayName("devrait retourner Optional.empty() quand le Type n'existe pas")
        void shouldReturnEmptyWhenTypeDoesNotExist() {
            PaymentMethodType type = PaymentMethodType.CREDIT_CARD;
            when(paymentMethodRepository.findByType(type)).thenReturn(Optional.empty());

            Optional<PaymentMethodDto> result = paymentMethodService.getPaymentMethodByType(type);

            assertThat(result).isEmpty();
            verify(paymentMethodRepository).findByType(type);
        }
        
        @Test
        @DisplayName("devrait retourner la liste des méthodes activées triées")
        void shouldReturnEnabledMethods() {
            List<PaymentMethod> entities = List.of(entity);
            List<PaymentMethodDto> dtos = List.of(dto);

            when(paymentMethodRepository.findByEnabledOrderByDisplayOrder(true)).thenReturn(entities);
            when(paymentMethodMapper.toDtoList(entities)).thenReturn(dtos);

            List<PaymentMethodDto> result = paymentMethodService.getEnabledPaymentMethods();

            assertThat(result).hasSize(1).containsExactly(dto);
            verify(paymentMethodRepository).findByEnabledOrderByDisplayOrder(true);
        }

        @Test
        @DisplayName("devrait retourner toutes les méthodes triées")
        void shouldReturnAllMethods() {
            List<PaymentMethod> entities = List.of(entity);
            List<PaymentMethodDto> dtos = List.of(dto);

            when(paymentMethodRepository.findAllByOrderByDisplayOrder()).thenReturn(entities);
            when(paymentMethodMapper.toDtoList(entities)).thenReturn(dtos);

            List<PaymentMethodDto> result = paymentMethodService.getAllPaymentMethods();

            assertThat(result).hasSize(1).containsExactly(dto);
            verify(paymentMethodRepository).findAllByOrderByDisplayOrder();
        }
    }

    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("devrait mettre à jour si la méthode existe déjà pour ce type")
        void shouldUpdateWhenMethodAlreadyExists() {
            PaymentMethodType type = PaymentMethodType.CREDIT_CARD;
            when(paymentMethodRepository.findByType(type)).thenReturn(Optional.of(entity));
            when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(entity);
            when(paymentMethodMapper.toDto(entity)).thenReturn(dto);

            PaymentMethodDto result = paymentMethodService.createOrUpdatePaymentMethod(
                    type, "Carte Mis à Jour", "Description", "icon.png", true, 2
            );

            assertThat(result).isEqualTo(dto);
            verify(paymentMethodRepository).findByType(type);
            verify(paymentMethodRepository).save(entity);
        }

        @Test
        @DisplayName("devrait créer une nouvelle méthode si le type n'existe pas encore")
        void shouldCreateNewWhenMethodDoesNotExist() {
            PaymentMethodType type = PaymentMethodType.CREDIT_CARD;
            when(paymentMethodRepository.findByType(type)).thenReturn(Optional.empty());
            when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(entity);
            when(paymentMethodMapper.toDto(entity)).thenReturn(dto);

            PaymentMethodDto result = paymentMethodService.createOrUpdatePaymentMethod(
                    type, "Nouvelle carte", "Description", "icon.png", true, 1
            );

            assertThat(result).isEqualTo(dto);
            verify(paymentMethodRepository).findByType(type);
            verify(paymentMethodRepository).save(any(PaymentMethod.class));
        }

        @Test
        @DisplayName("devrait modifier le statut 'enabled' quand l'ID existe")
        void shouldToggleStatusWhenIdExists() {
            when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(entity));
            when(paymentMethodRepository.save(entity)).thenReturn(entity);
            when(paymentMethodMapper.toDto(entity)).thenReturn(dto);

            PaymentMethodDto result = paymentMethodService.togglePaymentMethod(1L, false);

            assertThat(result).isEqualTo(dto);
            assertThat(entity.getEnabled()).isFalse();
            verify(paymentMethodRepository).findById(1L);
            verify(paymentMethodRepository).save(entity);
        }

        @Test
        @DisplayName("devrait lever une RuntimeException quand l'ID n'existe pas")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentMethodService.togglePaymentMethod(99L, true))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Méthode de paiement non trouvée");

            verify(paymentMethodRepository).findById(99L);
            verify(paymentMethodRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete tests")
    class DeleteTests {

        @Test
        @DisplayName("devrait appeler la suppression par ID sur le repository")
        void shouldDeleteById() {
            paymentMethodService.deletePaymentMethod(1L);

            verify(paymentMethodRepository).deleteById(1L);
        }
    }
}