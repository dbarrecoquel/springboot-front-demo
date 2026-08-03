package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.example.catalog.model.Category;
import com.example.catalog.repository.CategoryRepository;
import com.example.catalog.service.CategoryService;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
	@Mock
	private CategoryRepository categoryRepository;
	
	@InjectMocks
	private CategoryService categoryService;
	
	private Category category;
	private Category parent;
	
	@BeforeEach
	void setUp() {
		
		parent = new Category();
		parent.setId(1L);
		parent.setName("TEST");
		
		category = new Category();
		category.setId(2L);
		category.setName("TEST2");
		category.setParentCategory(parent);
		category.setParentCategoryId(parent.getId());
		
		parent.setSubCategories(List.of(category));
		
	}
	
	@Nested
	@DisplayName("Recherches simples")
	class FindTests {
		
		@Test
		@DisplayName("doit retrouver toutes les catégories")
		void shouldGetAllCategory() {
			
			List<Category> categories = List.of(parent,category);
			
			when(categoryRepository.findAll()).thenReturn(categories);
			
			List<Category> result = categoryService.getAllCategories();
			
			assertNotNull(result);
			assertEquals(2, result.size());
			
			verify(categoryRepository).findAll();
		}
		@Test
		@DisplayName("doit retrourner la catégorie parent")
		void shouldGetRootCategories() {
			
			List<Category> categories = List.of(parent);
			when(categoryRepository.findByParentCategoryIdIsNull()).thenReturn(categories);
			
			List<Category> result = categoryService.getRootCategories();
			assertNotNull(result);
			assertEquals(1,result.size());
			assertEquals("TEST",result.get(0).getName());
			
			verify(categoryRepository).findByParentCategoryIdIsNull();
		}
		@Test
		@DisplayName("ne doit retrourner aucune catégorie parent")
		void shouldNotGetRootCategories() {
			
			when(categoryRepository.findByParentCategoryIdIsNull()).thenReturn(null);
			
			List<Category> result = categoryService.getRootCategories();
			assertNull(result);
			
			verify(categoryRepository).findByParentCategoryIdIsNull();
		}
		@Test
		@DisplayName("doit retourner les sous catégories")
		void shouldGetSubCategories() {
			List<Category> categories = List.of(category);
			
			when(categoryRepository.findByParentCategoryId(parent.getId())).thenReturn(categories);
			
			List<Category> result = categoryService.getSubCategories(parent.getId());
			assertNotNull(result);
			assertEquals(1, result.size());
			
			verify(categoryRepository).findByParentCategoryId(parent.getId());
		}
		@Test
		@DisplayName("doit rien retourner en sous catégorie")
		void shouldNotGetSubCategories() {
			when(categoryRepository.findByParentCategoryId(category.getId())).thenReturn(null);
			
			List<Category> result = categoryService.getSubCategories(category.getId());
			assertNull(result);
			
			verify(categoryRepository).findByParentCategoryId(category.getId());
		}
		@Test
		@DisplayName("doit retourner la catégorie par id")
		void shouldGetCategoryById() {
			when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
			
			Optional<Category> result = categoryService.getCategoryById(parent.getId());
			assertThat(result).isPresent();
			
			verify(categoryRepository).findById(parent.getId());
		}
		@Test
		@DisplayName("doit retourner aucune catégorie par id")
		void shouldNotGetCategoryById() {
			when(categoryRepository.findById(parent.getId())).thenReturn(Optional.empty());
			
			Optional<Category> result = categoryService.getCategoryById(parent.getId());
			assertThat(result).isEmpty();
			
			verify(categoryRepository).findById(parent.getId());
		}
	}
	@Nested
	@DisplayName("Enregistrement catégorie")
	class SaveTests{
		
		@Test
		@DisplayName("doit enregistrer la catégorie")
		void shouldSaveCategory() {
			when(categoryRepository.save(category)).thenReturn(category);

	        Category result = categoryService.saveCategory(category);

	        assertThat(result).isNotNull();
	        assertThat(result.getId()).isEqualTo(category.getId());
	        assertThat(result.getName()).isEqualTo("TEST2");
	        assertThat(result.getParentCategoryId()).isEqualTo(parent.getId());

	        verify(categoryRepository).save(category);
		}
		
	}
	
}
