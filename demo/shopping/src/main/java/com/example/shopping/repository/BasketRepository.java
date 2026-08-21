package com.example.shopping.repository;

import com.example.shopping.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    
    /**
     * Trouver un panier par utilisateur (retourne le premier trouvé)
     */
    Optional<Basket> findByUserId(Long userId);
    
    /**
     * Trouver un panier par session
     */
    Optional<Basket> findBySessionId(String sessionId);
    
    /**
     * Trouver un panier par guestId
     */
    Optional<Basket> findByGuestId(String guestId);
    
    // ============================================
    // MÉTHODES AVEC STATUT (CRITIQUES)
    // ============================================
    
    /**
     * Trouver le panier ACTIF le plus récent pour un utilisateur
     * Retourne UN SEUL résultat (le plus récent)
     */
    @Query("SELECT b FROM Basket b WHERE b.userId = :userId AND b.status = :status ORDER BY b.createdAt DESC LIMIT 1")
    Optional<Basket> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Trouver le panier ACTIF le plus récent pour une session
     * Retourne UN SEUL résultat (le plus récent)
     */
    @Query("SELECT b FROM Basket b WHERE b.sessionId = :sessionId AND b.status = :status ORDER BY b.createdAt DESC LIMIT 1")
    Optional<Basket> findBySessionIdAndStatus(@Param("sessionId") String sessionId, @Param("status") String status);
    
    /**
     * Trouver tous les paniers d'un utilisateur avec un statut donné, triés par date (récents en premier)
     */
    List<Basket> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    
    /**
     * Trouver tous les paniers d'une session avec un statut donné
     */
    List<Basket> findBySessionIdAndStatusOrderByCreatedAtDesc(String sessionId, String status);
    
    // ============================================
    // MÉTHODES POUR LE NETTOYAGE
    // ============================================
    
    /**
     * Trouver TOUS les paniers d'un utilisateur, triés par date (récents en premier)
     * Utilisé pour nettoyer les doublons
     */
    List<Basket> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Compter les paniers actifs pour un utilisateur
     */
    long countByUserIdAndStatus(Long userId, String status);
    
    /**
     * Compter les paniers actifs pour une session
     */
    long countBySessionIdAndStatus(String sessionId, String status);
    
    // ============================================
    // MÉTHODES DE SUPPRESSION
    // ============================================
    
    /**
     * Supprimer tous les paniers complétés d'un utilisateur
     * (sauf le plus récent)
     */
    @Query("DELETE FROM Basket b WHERE b.userId = :userId AND b.status = :status AND b.id NOT IN (SELECT id FROM Basket WHERE userId = :userId AND status = :status ORDER BY createdAt DESC LIMIT 1)")
    void deleteCompletedBaskets(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * Supprimer tous les paniers d'une session
     */
    void deleteBySessionId(String sessionId);
}