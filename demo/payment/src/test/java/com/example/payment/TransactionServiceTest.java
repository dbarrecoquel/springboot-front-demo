package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.payment.dto.TransactionDto;
import com.example.payment.enums.PaymentMethodType;
import com.example.payment.enums.TransactionStatus;
import com.example.payment.mapper.TransactionMapper;
import com.example.payment.model.PaymentMethod;
import com.example.payment.model.Transaction;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.service.TransactionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService — Tests Unitaires")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private PaymentMethod paymentMethod;
    private Transaction transaction;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        paymentMethod = new PaymentMethod();
        paymentMethod.setId(10L);
        paymentMethod.setType(PaymentMethodType.CREDIT_CARD);

        transaction = Transaction.builder()
                .id(1L)
                .orderId(100L)
                .transactionNumber("TXN-123456")
                .paymentMethod(paymentMethod)
                .amount(99.99)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        transactionDto = new TransactionDto();
        transactionDto.setId(1L);
        transactionDto.setOrderId(100L);
        transactionDto.setTransactionNumber("TXN-123456");
        transactionDto.setAmount(99.99);
        transactionDto.setStatus(TransactionStatus.PENDING);
    }
    @Nested
    @DisplayName("Recherches simples")
    class FindTests {

        @Test
        @DisplayName("devrait retourner le DTO quand l'ID existe")
        void shouldReturnDtoWhenIdExists() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            Optional<TransactionDto> result = transactionService.getTransactionById(1L);

            assertThat(result).isPresent().contains(transactionDto);
            verify(transactionRepository).findById(1L);
        }

        @Test
        @DisplayName("devrait retourner Optional.empty() quand l'ID n'existe pas")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<TransactionDto> result = transactionService.getTransactionById(99L);

            assertThat(result).isEmpty();
            verify(transactionRepository).findById(99L);
            verify(transactionMapper, never()).toDto(any());
        }
    
        @Test
        @DisplayName("devrait retourner le DTO quand le numéro existe")
        void shouldReturnDtoWhenNumberExists() {
            when(transactionRepository.findByTransactionNumber("TXN-123456")).thenReturn(Optional.of(transaction));
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            Optional<TransactionDto> result = transactionService.getTransactionByNumber("TXN-123456");

            assertThat(result).isPresent().contains(transactionDto);
            verify(transactionRepository).findByTransactionNumber("TXN-123456");
        }

        @Test
        @DisplayName("devrait retourner Optional.empty() quand le numéro n'existe pas")
        void shouldReturnEmptyWhenNumberDoesNotExist() {
            when(transactionRepository.findByTransactionNumber("UNKNOWN")).thenReturn(Optional.empty());

            Optional<TransactionDto> result = transactionService.getTransactionByNumber("UNKNOWN");

            assertThat(result).isEmpty();
            verify(transactionRepository).findByTransactionNumber("UNKNOWN");
        }

        @Test
        @DisplayName("devrait retourner le DTO quand la commande existe")
        void shouldReturnDtoWhenOrderIdExists() {
            when(transactionRepository.findByOrderId(100L)).thenReturn(Optional.of(transaction));
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            Optional<TransactionDto> result = transactionService.getTransactionByOrderId(100L);

            assertThat(result).isPresent().contains(transactionDto);
            verify(transactionRepository).findByOrderId(100L);
        }

        @Test
        @DisplayName("devrait retourner Optional.empty() quand la commande n'existe pas")
        void shouldReturnEmptyWhenOrderIdDoesNotExist() {
            when(transactionRepository.findByOrderId(999L)).thenReturn(Optional.empty());

            Optional<TransactionDto> result = transactionService.getTransactionByOrderId(999L);

            assertThat(result).isEmpty();
            verify(transactionRepository).findByOrderId(999L);
        }

        @Test
        @DisplayName("devrait retourner une page de transactions filtrées par statut")
        void shouldReturnPageOfTransactionsByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> entityPage = new PageImpl<>(List.of(transaction), pageable, 1);

            when(transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING, pageable))
                    .thenReturn(entityPage);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            Page<TransactionDto> result = transactionService.getTransactionsByStatus(TransactionStatus.PENDING, pageable);

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(transactionDto);
            verify(transactionRepository).findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING, pageable);
        }
    }


    @Test
    @DisplayName("devrait retourner une page avec toutes les transactions")
    void shouldReturnPageOfAllTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> entityPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(entityPage);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        Page<TransactionDto> result = transactionService.getAllTransactions(pageable);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(transactionDto);
        verify(transactionRepository).findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Nested
    @DisplayName("save tests")
    class SaveTests {

        @Test
        @DisplayName("devrait créer et retourner la transaction quand la méthode de paiement existe")
        void shouldCreateTransactionWhenPaymentMethodExists() {
            when(paymentMethodRepository.findById(10L)).thenReturn(Optional.of(paymentMethod));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            TransactionDto result = transactionService.createTransaction(100L, 10L, 99.99);

            assertThat(result).isNotNull().isEqualTo(transactionDto);
            verify(paymentMethodRepository).findById(10L);
            verify(transactionRepository).save(any(Transaction.class));
            verify(transactionMapper).toDto(transaction);
        }

        @Test
        @DisplayName("devrait lever une exception quand la méthode de paiement n'existe pas")
        void shouldThrowExceptionWhenPaymentMethodNotFound() {
            when(paymentMethodRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.createTransaction(100L, 99L, 99.99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Méthode de paiement non trouvée");

            verify(paymentMethodRepository).findById(99L);
            verify(transactionRepository, never()).save(any());
        }
        @Test
        @DisplayName("devrait mettre à jour le statut et la note sans completedAt si statut != COMPLETED")
        void shouldUpdateStatusAndNotes() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(transaction)).thenReturn(transaction);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            TransactionDto result = transactionService.updateTransactionStatus(1L, TransactionStatus.FAILED, "Echec paiement");

            assertThat(result).isNotNull();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(transaction.getNotes()).isEqualTo("Echec paiement");
            assertThat(transaction.getCompletedAt()).isNull();
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("devrait définir completedAt si le statut mis à jour est COMPLETED")
        void shouldSetCompletedAtWhenStatusIsCompleted() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(transaction)).thenReturn(transaction);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED, "Paiement validé");

            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(transaction.getCompletedAt()).isNotNull();
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("devrait lever une exception si la transaction n'existe pas")
        void shouldThrowExceptionWhenNotFound() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.updateTransactionStatus(99L, TransactionStatus.COMPLETED, "Note"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Transaction non trouvée");

            verify(transactionRepository, never()).save(any());
        }
        @Test
        @DisplayName("devrait finaliser la transaction avec la référence externe")
        void shouldCompleteTransaction() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(transaction)).thenReturn(transaction);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            TransactionDto result = transactionService.completeTransaction(1L, "EXT-PAY-987");

            assertThat(result).isNotNull();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(transaction.getExternalReference()).isEqualTo("EXT-PAY-987");
            assertThat(transaction.getCompletedAt()).isNotNull();
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("devrait lever une exception si la transaction n'existe pas")
        void shouldThrowExceptionWhenNotFoundComplete() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.completeTransaction(99L, "EXT-REF"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Transaction non trouvée");
        }

        @Test
        @DisplayName("devrait passer la transaction en FAILED avec le message d'erreur")
        void shouldFailTransaction() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(transaction)).thenReturn(transaction);
            when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

            TransactionDto result = transactionService.failTransaction(1L, "Fonds insuffisants");

            assertThat(result).isNotNull();
            assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(transaction.getErrorMessage()).isEqualTo("Fonds insuffisants");
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("devrait lever une exception si la transaction n'existe pas")
        void shouldThrowExceptionWhenNotFoundFail() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.failTransaction(99L, "Erreur"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Transaction non trouvée");
        }
    }

    
}