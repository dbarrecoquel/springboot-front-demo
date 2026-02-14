package com.example.frontrest.controller;

import com.example.catalog.dto.CategoryDto;
import com.example.catalog.model.Category;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.model.mapper.CategoryMapper;
import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.events.model.CategoryViewEvent;
import com.example.events.producer.CategoryEventProducer;
import com.example.frontrest.models.CategoryResponse;
import com.example.product.mapper.ProductMapper;
import com.example.product.model.dto.ProductDto;
import com.example.product.service.ProductService;
import com.example.user.model.User;
import com.example.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CatalogNavigationController {
    
    private final CategoryService categoryService;
    private final ProductCategoryAssignmentService assignmentService;
    private final ProductService productService;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final UserService userService;
    private final CategoryEventProducer categoryEventProducer;
    
    // Injection par constructeur avec @Autowired optionnel
    public CatalogNavigationController(CategoryService categoryService,
                                      ProductCategoryAssignmentService assignmentService,
                                      ProductService productService,
                                      CategoryMapper categoryMapper,
                                      ProductMapper productMapper,
                                      UserService userService,
                                      Optional<CategoryEventProducer> categoryEventProducer) {
        this.categoryService = categoryService;
        this.assignmentService = assignmentService;
        this.productService = productService;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.userService = userService;
        this.categoryEventProducer = categoryEventProducer.orElse(null);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(
                categoryService.getRootCategories()
                        .stream()
                        .map(categoryMapper::toDto)
                        .toList()
        );
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> viewCategory(@PathVariable Long id,
                                                         Authentication authentication,
                                                         HttpServletRequest request,
                                                         HttpSession session) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide : " + id));
        
        CategoryDto categoryDto = categoryMapper.toDto(category);
        
        List<CategoryDto> subCategories = categoryService.getSubCategories(id).stream()
                .map(categoryMapper::toDto)
                .toList();
        
        List<ProductDto> products = assignmentService.getAssignmentsByCategoryId(id)
                .stream()
                .map(ProductCategoryAssignment::getProductId)
                .map(productService::getProductById)
                .flatMap(Optional::stream)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        
        List<Category> breadcrumbCategories = buildBreadcrumb(category);
        List<CategoryDto> breadcrumb = breadcrumbCategories
                .stream()
                .map(categoryMapper::toDto)
                .toList();
        
        // Envoyer l'événement Kafka (si disponible)
        if (categoryEventProducer != null) {
            sendCategoryViewEvent(category, subCategories.size(), products.size(), 
                                breadcrumbCategories, authentication, request, session);
        }
        
        CategoryResponse response = new CategoryResponse(
                categoryDto,
                subCategories,
                products,
                breadcrumb
        );
        
        return ResponseEntity.ok(response);
    }
    
    private void sendCategoryViewEvent(Category category, int subcategoriesCount, int productsCount,
                                      List<Category> breadcrumb, Authentication authentication,
                                      HttpServletRequest request, HttpSession session) {
        try {
            int depthLevel = breadcrumb.size() - 1;
            
            String breadcrumbPath = breadcrumb.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(" > "));
            
            Long parentCategoryId = category.getParentCategoryId();
            String parentCategoryName = null;
            if (parentCategoryId != null) {
                parentCategoryName = categoryService.getCategoryById(parentCategoryId)
                        .map(Category::getName)
                        .orElse(null);
            }
            
            CategoryViewEvent event = new CategoryViewEvent(
                category.getId(),
                category.getName(),
                parentCategoryId,
                parentCategoryName,
                depthLevel,
                subcategoriesCount,
                productsCount,
                getUserId(authentication),
                session.getId(),
                getUserEmail(authentication)
            );
            
            event.setIpAddress(getClientIp(request));
            event.setUserAgent(request.getHeader("User-Agent"));
            event.setBreadcrumbPath(breadcrumbPath);
            
            categoryEventProducer.sendCategoryViewEvent(event);
            
        } catch (Exception e) {
            System.err.println("Error sending CategoryViewEvent: " + e.getMessage());
        }
    }
    
    private List<Category> buildBreadcrumb(Category category) {
        List<Category> breadcrumb = new ArrayList<>();
        Category current = category;
        
        // Protection contre les boucles infinies
        int maxDepth = 10; // Limite de profondeur
        int depth = 0;
        
        while (current != null && depth < maxDepth) {
            breadcrumb.add(0, current);
            
            if (current.getParentCategoryId() != null) {
                // Vérifier qu'on ne reboucle pas sur une catégorie déjà visitée
                final Long parentId = current.getParentCategoryId();
                boolean alreadyVisited = breadcrumb.stream()
                        .anyMatch(c -> c.getId().equals(parentId));
                
                if (alreadyVisited) {
                    System.err.println("⚠️ Circular reference detected in category hierarchy for category ID: " + current.getId());
                    break;
                }
                
                current = categoryService.getCategoryById(parentId).orElse(null);
            } else {
                current = null;
            }
            
            depth++;
        }
        
        if (depth >= maxDepth) {
            System.err.println("⚠️ Maximum breadcrumb depth reached for category ID: " + category.getId());
        }
        
        return breadcrumb;
    }
    
    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return userService.findByEmail(authentication.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
    
    private String getUserEmail(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}