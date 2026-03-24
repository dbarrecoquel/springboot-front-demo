package com.example.frontrest.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalog.dto.CategoryDto;
import com.example.catalog.model.Category;
import com.example.catalog.model.ProductCategoryAssignment;
import com.example.catalog.model.mapper.CategoryMapper;
import com.example.catalog.service.CategoryService;
import com.example.catalog.service.ProductCategoryAssignmentService;
import com.example.events.model.CategoryViewEvent;
import com.example.events.producer.EventProducer;
import com.example.frontrest.models.CategoryResponse;
import com.example.product.mapper.ProductMapper;
import com.example.product.model.Product;
import com.example.product.model.dto.ProductDto;
import com.example.product.service.ProductService;
import com.example.user.model.User;
import com.example.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
@RestController
@RequestMapping("/api")
public class CatalogNavigationController {
    
    private final CategoryService categoryService;
    private final ProductCategoryAssignmentService assignmentService;
    private final ProductService productService;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final UserService userService;
    private final EventProducer categoryEventProducer;
    
    // Injection par constructeur avec @Autowired optionnel
    public CatalogNavigationController(CategoryService categoryService,
                                      ProductCategoryAssignmentService assignmentService,
                                      ProductService productService,
                                      CategoryMapper categoryMapper,
                                      ProductMapper productMapper,
                                      UserService userService,
                                      Optional<EventProducer> categoryEventProducer) {
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
        
     
        
        List<Category> breadcrumbCategories = buildBreadcrumb(category);
        List<CategoryDto> breadcrumb = breadcrumbCategories
                .stream()
                .map(categoryMapper::toDto)
                .toList();
        
        // Envoyer l'événement Kafka (si disponible)
        // Controller → sendCategoryViewEvent() → CategoryViewEvent (constructeur + setters) → Kafka Producer → JSON
        if (categoryEventProducer != null) {
            sendCategoryViewEvent(category, subCategories.size(), 
                                breadcrumbCategories, authentication, request, session);
        }
        
        CategoryResponse response = new CategoryResponse(
                categoryDto,
                subCategories,
                breadcrumb
        );
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/categories/{id}/{subcat_id}")
    public ResponseEntity<Page<ProductDto>>  viewSubCategory(@PathVariable Long id,
    														@PathVariable Long subcat_id,
                                                         Authentication authentication,
                                                         HttpServletRequest request,
                                                         HttpSession session,
                                                         @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) {
    	
    	return ResponseEntity.ok(getProductListByCategoryId(subcat_id,pageable));
    }
    
    @GetMapping("/categories/{id}/products")
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @PathVariable Long id, 
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable) { // Reçoit automatiquement ?page=0&size=10
        
        return ResponseEntity.ok(getProductListByCategoryId(id, pageable));
    }
    @GetMapping("/categories/{id}/products/{sku}")
    public ResponseEntity<ProductDto> getProductBySku(@PathVariable Long id,@PathVariable String sku){
    	Product p = productService.getProductBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product invalide : " + sku));
    	 
    	return ResponseEntity.ok(productMapper.toDto(p));
    }
    @GetMapping("/products")
    public ResponseEntity<Map<String,Object>> getAllProducts(
    	@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice) {


    return ResponseEntity.ok(getProductsReponse(page, size, sortBy, direction, search, minPrice, maxPrice));
}
    
    private Map<String, Object> getProductsReponse(int page,
            int size,
            String sortBy,
            String direction,
            String search,
            Double minPrice,
            Double maxPrice){
    	 Sort sort = direction.equalsIgnoreCase("desc")
    	            ? Sort.by(sortBy).descending()
    	            : Sort.by(sortBy).ascending();

    	    Pageable pageable = PageRequest.of(page, size, sort);

    	    Page<Product> pageResult = productService.findWithFilters(search, minPrice, maxPrice, pageable);

    	    List<ProductDto> content = pageResult.getContent()
    	            .stream()
    	            .map(productMapper::toDto)
    	            .collect(Collectors.toList());

    	    Map<String, Object> response = new HashMap<>();
    	    response.put("content", content);
    	    response.put("page", pageResult.getNumber());
    	    response.put("size", pageResult.getSize());
    	    response.put("totalElements", pageResult.getTotalElements());
    	    response.put("totalPages", pageResult.getTotalPages());
    	    response.put("last", pageResult.isLast());
    	    
    	    return response;
    	
    }
    
    
    private void sendCategoryViewEvent(Category category, int subcategoriesCount,
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
    private Page<ProductDto> getProductListByCategoryId(Long id, Pageable pageable) {
        // 1. Récupérer uniquement la page d'assignations (ex: 20 lignes)
        Page<ProductCategoryAssignment> assignmentPage = 
            assignmentService.getAssignmentsByCategoryId(id, pageable);

        // 2. Extraire la liste des IDs de produits de cette page
        List<Long> productIds = assignmentPage.getContent().stream()
                .map(ProductCategoryAssignment::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 3. Récupérer TOUS les produits concernés en UNE SEULE requête SQL
        Map<Long, ProductDto> productsMap = productService.findAllByIds(productIds)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toMap(ProductDto::getId, dto -> dto));

        // 4. Transformer la Page d'assignations en Page de DTOs en utilisant la Map
        return assignmentPage.map(assignment -> productsMap.get(assignment.getProductId()));
    }
    
}