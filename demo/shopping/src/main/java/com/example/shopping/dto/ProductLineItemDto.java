package com.example.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.product.dto.ProductDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductLineItemDto {
    
    private Long id;
    private Long basketId;
    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    
    // Objet Product complet pour faciliter l'affichage dans les templates
    private ProductDto product;
    
    /**
     * Calculer le sous-total de cette ligne
     */
    public Double getSubtotal() {
        if (unitPrice == null || quantity == null) {
            return 0.0;
        }
        return unitPrice * quantity;
    }
    
    /**
     * Calculer la taxe sur cette ligne (exemple: 20%)
     */
    public Double getTax() {
        if (getSubtotal() == null) {
            return 0.0;
        }
        return getSubtotal() * 0.20;
    }
    
    /**
     * Calculer le total TTC pour cette ligne
     */
    public Double getTotal() {
        return getSubtotal() + getTax();
    }
}