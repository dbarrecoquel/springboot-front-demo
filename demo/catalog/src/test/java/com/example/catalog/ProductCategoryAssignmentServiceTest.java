package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import java.util.List;

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

import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.repository.ProductCategoryAssignmentRepository;
import com.example.catalog.service.ProductCategoryAssignmentService;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryAssignmentServiceTest {
	@Mock
	private ProductCategoryAssignmentRepository assignmentRepository;
	
	@InjectMocks
	private ProductCategoryAssignmentService assignmentService;
	
	private ProductCategoryAssignment assignment;
    private Long productId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        productId = 10L;
        categoryId = 20L;
        assignment = new ProductCategoryAssignment(productId, categoryId);
    }
    
    @Nested
    @DisplayName("Recherches simples")
    class ReadTests {

        @Test
        @DisplayName("getAssignmentsByProductId - doit retourner la liste des assignations")
        void shouldGetAssignmentsByProductId() {
            
            when(assignmentRepository.findByProductId(productId)).thenReturn(List.of(assignment));

            
            List<ProductCategoryAssignment> result = assignmentService.getAssignmentsByProductId(productId);

            assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(assignment);

            verify(assignmentRepository).findByProductId(productId);
        }

        @Test
        @DisplayName("getAssignmentsByCategoryId - doit retourner une page d'assignations")
        void shouldGetAssignmentsByCategoryId() {
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProductCategoryAssignment> page = new PageImpl<>(List.of(assignment), pageable, 1);
            when(assignmentRepository.findByCategoryId(categoryId, pageable)).thenReturn(page);

            
            Page<ProductCategoryAssignment> result = assignmentService.getAssignmentsByCategoryId(categoryId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1).containsExactly(assignment);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(assignmentRepository).findByCategoryId(categoryId, pageable);
        }

        @Test
        @DisplayName("getAllAssignments - doit retourner toutes les assignations")
        void shouldGetAllAssignments() {
            
            when(assignmentRepository.findAll()).thenReturn(List.of(assignment));

            List<ProductCategoryAssignment> result = assignmentService.getAllAssignments();

            assertThat(result)
                .isNotNull()
                .hasSize(1)
                .containsExactly(assignment);

            verify(assignmentRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Tests d'assignation (assignProductToCategory)")
    class AssignTests {

        @Test
        @DisplayName("doit créer et sauvegarder l'assignation si elle n'existe pas encore")
        void shouldAssignProductToCategory_WhenNotAlreadyAssigned() {
           
            when(assignmentRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(false);
            when(assignmentRepository.save(any(ProductCategoryAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            ProductCategoryAssignment result = assignmentService.assignProductToCategory(productId, categoryId);

            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getCategoryId()).isEqualTo(categoryId);

            verify(assignmentRepository).existsByProductIdAndCategoryId(productId, categoryId);
            verify(assignmentRepository).save(any(ProductCategoryAssignment.class));
        }

        @Test
        @DisplayName("doit retourner null et ne pas sauvegarder si le produit est déjà assigné")
        void shouldReturnNull_WhenAlreadyAssigned() {
           
            when(assignmentRepository.existsByProductIdAndCategoryId(productId, categoryId)).thenReturn(true);

            ProductCategoryAssignment result = assignmentService.assignProductToCategory(productId, categoryId);

            assertThat(result).isNull();

            verify(assignmentRepository).existsByProductIdAndCategoryId(productId, categoryId);
            verify(assignmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de suppression")
    class DeleteTests {

        @Test
        @DisplayName("removeProductFromCategory - doit appeler deleteByProductIdAndCategoryId sur le repository")
        void shouldRemoveProductFromCategory() {
          
            assignmentService.removeProductFromCategory(productId, categoryId);

            
            verify(assignmentRepository).deleteByProductIdAndCategoryId(productId, categoryId);
        }
    }
}
