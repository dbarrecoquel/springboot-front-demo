package com.example.shopping.model;

import java.time.LocalDateTime;

import com.example.product.model.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product_line_items")
public class ProductLineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "basket_id", nullable = false)
    private Long basketId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double unitPrice; // Prix au moment de l'ajout au panier
    
    // Relation pour faciliter les requêtes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", insertable = false, updatable = false)
    private Basket basket;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Constructors
    public ProductLineItem() {}
    
    public ProductLineItem(Long basketId, Long productId, Integer quantity, Double unitPrice) {
        this.basketId = basketId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBasketId() {
        return basketId;
    }
    
    public void setBasketId(Long basketId) {
        this.basketId = basketId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public Basket getBasket() {
        return basket;
    }
    
    public void setBasket(Basket basket) {
        this.basket = basket;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    // Méthodes utiles
    public Double getSubtotal() {
        return unitPrice * quantity;
    }

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}