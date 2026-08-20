package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.payment.dto.TransactionDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.enums.TransactionStatus;
import com.example.payment.service.PaymentMethodService;
import com.example.payment.service.PaymentService;
import com.example.payment.service.TransactionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — Tests Unitaires")
class PaymentServiceTest {

    @Mock
    private PaymentMethodService paymentMethodService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentMethodDto codMethodDto;
    private TransactionDto initialTransactionDto;
    private TransactionDto updatedTransactionDto;

    @BeforeEach
    void setUp() {
        codMethodDto = new PaymentMethodDto();
        codMethodDto.setId(1L);
        codMethodDto.setType(PaymentMethodType.COD);
        codMethodDto.setEnabled(true);

        initialTransactionDto = new TransactionDto();
        initialTransactionDto.setId(10L);
        initialTransactionDto.setOrderId(100L);
        initialTransactionDto.setTransactionNumber("TXN-12345");
        initialTransactionDto.setStatus(TransactionStatus.PENDING);

        updatedTransactionDto = new TransactionDto();
        updatedTransactionDto.setId(10L);
        updatedTransactionDto.setOrderId(100L);
        updatedTransactionDto.setTransactionNumber("TXN-12345");
        updatedTransactionDto.setStatus(TransactionStatus.PENDING);
    }

    @Nested
    @DisplayName("initiateCODPayment()")
    class InitiateCODPaymentTests {

        @Test
        @DisplayName("devrait initier le paiement COD avec succès quand la méthode existe et est activée")
        void shouldInitiateCODPaymentSuccessfully() {
            when(paymentMethodService.getPaymentMethodByType(PaymentMethodType.COD))
                    .thenReturn(Optional.of(codMethodDto));
            when(transactionService.createTransaction(100L, 1L, 50.0))
                    .thenReturn(initialTransactionDto);
            when(transactionService.updateTransactionStatus(10L, TransactionStatus.PENDING, "En attente de livraison"))
                    .thenReturn(updatedTransactionDto);

            TransactionDto result = paymentService.initiateCODPayment(100L, 50.0);

            assertThat(result).isNotNull().isEqualTo(updatedTransactionDto);
            verify(paymentMethodService).getPaymentMethodByType(PaymentMethodType.COD);
            verify(transactionService).createTransaction(100L, 1L, 50.0);
            verify(transactionService).updateTransactionStatus(10L, TransactionStatus.PENDING, "En attente de livraison");
        }

        @Test
        @DisplayName("devrait lever une exception quand la méthode COD n'est pas configurée")
        void shouldThrowExceptionWhenCODNotConfigured() {
            when(paymentMethodService.getPaymentMethodByType(PaymentMethodType.COD))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.initiateCODPayment(100L, 50.0))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Méthod COD non configurée");

            verify(paymentMethodService).getPaymentMethodByType(PaymentMethodType.COD);
            verify(transactionService, never()).createTransaction(any(), any(), any());
        }

        @Test
        @DisplayName("devrait lever une exception quand le paiement COD est désactivé")
        void shouldThrowExceptionWhenCODIsDisabled() {
            codMethodDto.setEnabled(false);
            when(paymentMethodService.getPaymentMethodByType(PaymentMethodType.COD))
                    .thenReturn(Optional.of(codMethodDto));

            assertThatThrownBy(() -> paymentService.initiateCODPayment(100L, 50.0))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Le paiement à la livraison n'est pas activée");

            verify(paymentMethodService).getPaymentMethodByType(PaymentMethodType.COD);
            verify(transactionService, never()).createTransaction(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("confirmCODPayment()")
    class ConfirmCODPaymentTests {

        @Test
        @DisplayName("devrait confirmer le paiement quand la transaction existe")
        void shouldConfirmCODPaymentSuccessfully() {
            TransactionDto completedTransactionDto = new TransactionDto();
            completedTransactionDto.setId(10L);
            completedTransactionDto.setStatus(TransactionStatus.COMPLETED);

            when(transactionService.getTransactionById(10L))
                    .thenReturn(Optional.of(initialTransactionDto));
            when(transactionService.completeTransaction(10L, null))
                    .thenReturn(completedTransactionDto);

            TransactionDto result = paymentService.confirmCODPayment(10L);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            verify(transactionService).getTransactionById(10L);
            verify(transactionService).completeTransaction(10L, null);
        }

        @Test
        @DisplayName("devrait lever une exception quand la transaction n'existe pas")
        void shouldThrowExceptionWhenTransactionNotFound() {
            when(transactionService.getTransactionById(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.confirmCODPayment(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Transaction non trouvée");

            verify(transactionService).getTransactionById(99L);
            verify(transactionService, never()).completeTransaction(any(), any());
        }
    }

    @Nested
    @DisplayName("cancelTransaction()")
    class CancelTransactionTests {

        @Test
        @DisplayName("devrait annuler la transaction avec le motif spécifié")
        void shouldCancelTransactionSuccessfully() {
            TransactionDto cancelledDto = new TransactionDto();
            cancelledDto.setId(10L);
            cancelledDto.setStatus(TransactionStatus.CANCELLED);

            when(transactionService.updateTransactionStatus(10L, TransactionStatus.CANCELLED, "Annulation client"))
                    .thenReturn(cancelledDto);

            TransactionDto result = paymentService.cancelTransaction(10L, "Annulation client");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
            verify(transactionService).updateTransactionStatus(10L, TransactionStatus.CANCELLED, "Annulation client");
        }
    }
}