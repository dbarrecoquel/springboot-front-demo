package com.example.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService — Tests Unitaires")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Clavier Mécanique");
        product1.setSku("KEY-MEC-001");
        product1.setPrice(new BigDecimal("89.99").doubleValue());

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Souris Sans Fil");
        product2.setSku("MOU-WIR-002");
        product2.setPrice(new BigDecimal("49.99").doubleValue());
    }

    @Nested
    @DisplayName("Recherches simples")
    class FindTests {

        @Test
        @DisplayName("getAllProducts - doit retourner la liste de tous les produits")
        void shouldGetAllProducts() {
            // GIVEN
            when(productRepository.findAll()).thenReturn(List.of(product1, product2));

            // WHEN
            List<Product> result = productService.getAllProducts();

            // THEN
            assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(product1, product2);

            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("getProductById - doit retourner le produit si trouvé")
        void shouldGetProductById_WhenFound() {
            // GIVEN
            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

            // WHEN
            Optional<Product> result = productService.getProductById(1L);

            // THEN
            assertThat(result).isPresent().contains(product1);
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("getProductById - doit retourner Optional.empty() si non trouvé")
        void shouldGetProductById_WhenNotFound() {
            // GIVEN
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            // WHEN
            Optional<Product> result = productService.getProductById(99L);

            // THEN
            assertThat(result).isEmpty();
            verify(productRepository).findById(99L);
        }

        @Test
        @DisplayName("getProductBySku - doit retourner le produit si le SKU existe")
        void shouldGetProductBySku_WhenFound() {
            // GIVEN
            when(productRepository.findBySku("KEY-MEC-001")).thenReturn(Optional.of(product1));

            // WHEN
            Optional<Product> result = productService.getProductBySku("KEY-MEC-001");

            // THEN
            assertThat(result).isPresent().contains(product1);
            verify(productRepository).findBySku("KEY-MEC-001");
        }

        @Test
        @DisplayName("getProductBySku - doit retourner Optional.empty() si le SKU n'existe pas")
        void shouldGetProductBySku_WhenNotFound() {
            // GIVEN
            when(productRepository.findBySku("UNKNOWN-SKU")).thenReturn(Optional.empty());

            // WHEN
            Optional<Product> result = productService.getProductBySku("UNKNOWN-SKU");

            // THEN
            assertThat(result).isEmpty();
            verify(productRepository).findBySku("UNKNOWN-SKU");
        }

        @Test
        @DisplayName("findAllByIds - doit retourner les produits correspondants aux IDs")
        void shouldFindAllByIds() {
            // GIVEN
            List<Long> ids = List.of(1L, 2L);
            when(productRepository.findAllById(ids)).thenReturn(List.of(product1, product2));

            // WHEN
            List<Product> result = productService.findAllByIds(ids);

            // THEN
            assertThat(result)
                .isNotNull()
                .hasSize(2)
                .containsExactly(product1, product2);

            verify(productRepository).findAllById(ids);
        }
    }

    @Nested
    @DisplayName("Tests de recherche filtrée")
    class SearchTests {

        @Test
        @DisplayName("findWithFilters - doit transmettre le mot-clé et les filtres de prix")
        void shouldFindWithFilters_WhenKeywordIsProvided() {
            // GIVEN
            String keyword = "Clavier";
            Double minPrice = 50.0;
            Double maxPrice = 150.0;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(product1), pageable, 1);

            when(productRepository.findWithFilters(keyword, minPrice, maxPrice, pageable))
                .thenReturn(expectedPage);

            // WHEN
            Page<Product> result = productService.findWithFilters(keyword, minPrice, maxPrice, pageable);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1).containsExactly(product1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(productRepository).findWithFilters(keyword, minPrice, maxPrice, pageable);
        }

        @Test
        @DisplayName("findWithFilters - doit remplacer un mot-clé null par une chaîne vide \"\"")
        void shouldFindWithFilters_WhenKeywordIsNull() {
            // GIVEN
            Double minPrice = 10.0;
            Double maxPrice = 200.0;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> expectedPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

            // Remarquez l'attente de "" au lieu de null
            when(productRepository.findWithFilters(eq(""), eq(minPrice), eq(maxPrice), eq(pageable)))
                .thenReturn(expectedPage);

            // WHEN
            Page<Product> result = productService.findWithFilters(null, minPrice, maxPrice, pageable);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);

            // Vérification clé : le repository a reçu "" à la place de null
            verify(productRepository).findWithFilters("", minPrice, maxPrice, pageable);
        }
    }

    @Nested
    @DisplayName("Tests de modification et suppression")
    class CUDTests {

        @Test
        @DisplayName("saveProduct - doit sauvegarder et retourner le produit")
        void shouldSaveProduct() {
            // GIVEN
            when(productRepository.save(product1)).thenReturn(product1);

            // WHEN
            Product result = productService.saveProduct(product1);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Clavier Mécanique");

            verify(productRepository).save(product1);
        }

        @Test
        @DisplayName("deleteProduct - doit appeler deleteById sur le repository")
        void shouldDeleteProduct() {
            // WHEN
            productService.deleteProduct(1L);

            // THEN
            verify(productRepository).deleteById(1L);
        }
    }
}