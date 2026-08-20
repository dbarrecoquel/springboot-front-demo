package com.example.product.repository;

import com.example.product.dto.ProductStockDto;
import com.example.product.dto.ProductStockWithWarehouseProjection;
import com.example.product.enums.StockStatus;
import com.example.product.model.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
    
    Optional<ProductStock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    
    List<ProductStock> findByProductId(Long productId);
    
    List<ProductStock> findByWarehouseId(Long warehouseId);
    
    List<ProductStock> findByWarehouseIdAndStatus(Long warehouseId, String status);
    
    /**
     * Vérifier si un produit est en stock avec quantité suffisante
     */
    @Query("SELECT COUNT(ps) > 0 FROM ProductStock ps WHERE ps.productId = :productId AND ps.warehouseId = :warehouseId AND ps.quantity >= :quantity")
    boolean isProductInStock(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("quantity") Integer quantity);
    
    /**
     * Trouver les entrepôts qui contiennent un produit
     */
    @Query("SELECT DISTINCT ps.warehouseId FROM ProductStock ps WHERE ps.productId = :productId")
    List<Long> findWarehouseIdsWithProduct(@Param("productId") Long productId);
    
    /**
     * Filtrer et paginer les stocks
     */
    @Query("SELECT ps FROM ProductStock ps WHERE " +
           "(:warehouseId IS NULL OR ps.warehouseId = :warehouseId) AND " +
           "(:status IS NULL OR ps.status = :status)")
    Page<ProductStock> filterStocks(
        @Param("warehouseId") Long warehouseId,
        @Param("status") String status,
        Pageable pageable);
    
    /**
     * Filtrer par entrepôt uniquement
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.warehouseId = :warehouseId")
    Page<ProductStock> findByWarehouseIdPaginated(@Param("warehouseId") Long warehouseId, Pageable pageable);
    
    /**
     * Filtrer par statut uniquement
     */
    @Query("SELECT ps FROM ProductStock ps WHERE ps.status = :status")
    Page<ProductStock> findByStatusPaginated(@Param("status") String status, Pageable pageable);
    
    @Query(value = """
    	    SELECT 
    	        ps.product_id AS productId, 
    	        ps.warehouse_id AS warehouseId, 
    	        w.name AS warehouseName, 
    	        w.code AS warehouseCode, 
    	        ps.quantity AS quantity, 
    	        ps.minimum_stock AS minimumStock, 
    	        CAST(ps.status AS VARCHAR) AS status, 
    	        CASE WHEN ps.quantity > 0 THEN true ELSE false END AS inStock 
    	    FROM product_stocks ps 
    	    LEFT JOIN warehouses w ON ps.warehouse_id = w.id 
    	    WHERE (:warehouseId IS NULL OR ps.warehouse_id = :warehouseId) 
    	      AND (:status IS NULL OR ps.status = :status)
    	    """, 
    	    countQuery = """
    	    SELECT count(*) FROM product_stocks ps 
    	    WHERE (:warehouseId IS NULL OR ps.warehouse_id = :warehouseId) 
    	      AND (:status IS NULL OR ps.status = :status)
    	    """,
    	    nativeQuery = true)
     Page<ProductStockWithWarehouseProjection> filterStocksWithWarehouse(
         @Param("warehouseId") Long warehouseId,
         @Param("status") StockStatus status,
         Pageable pageable);
}