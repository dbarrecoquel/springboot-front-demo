package com.example.frontrest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour HealthController
 * Ces tests ne nécessitent pas de contexte Spring
 */
class HealthControllerTest {
    
    private HealthController healthController;
    
    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }
    
    /* ===================== HEALTH ENDPOINT ===================== */
    
    @Test
    void testHealth_ReturnsOk() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    void testHealth_ReturnsCorrectStatus() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertTrue(body.containsKey("status"));
        assertEquals("UP", body.get("status"));
    }
    
    @Test
    void testHealth_ReturnsServiceName() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertTrue(body.containsKey("service"));
        assertEquals("FrontRest API", body.get("service"));
    }
    
    @Test
    void testHealth_ReturnsPort() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertTrue(body.containsKey("port"));
        assertEquals(8082, body.get("port"));
    }
    
    @Test
    void testHealth_ReturnsTimestamp() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertNotNull(body.get("timestamp"));
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
    }
    
    @Test
    void testHealth_TimestampIsRecent() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertNotNull(body);
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");
        assertNotNull(timestamp);
        
        // Le timestamp doit être entre before et after
        assertTrue(timestamp.isAfter(before) || timestamp.isEqual(before), 
            "Timestamp should be after or equal to 'before'");
        assertTrue(timestamp.isBefore(after) || timestamp.isEqual(after), 
            "Timestamp should be before or equal to 'after'");
    }
    
    @Test
    void testHealth_ContainsAllRequiredFields() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        
        // Vérifier que toutes les clés sont présentes
        assertTrue(body.containsKey("status"), "Response should contain 'status'");
        assertTrue(body.containsKey("service"), "Response should contain 'service'");
        assertTrue(body.containsKey("timestamp"), "Response should contain 'timestamp'");
        assertTrue(body.containsKey("port"), "Response should contain 'port'");
        
        // Vérifier qu'il n'y a que 4 champs
        assertEquals(4, body.size(), "Response should contain exactly 4 fields");
    }
    
    @Test
    void testHealth_ResponseBodyIsNotNull() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertNotNull(response.getBody(), "Response body should not be null");
    }
    
    @Test
    void testHealth_AllValuesAreNotNull() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertNotNull(body.get("status"), "status should not be null");
        assertNotNull(body.get("service"), "service should not be null");
        assertNotNull(body.get("timestamp"), "timestamp should not be null");
        assertNotNull(body.get("port"), "port should not be null");
    }
    
    @Test
    void testHealth_ValueTypes() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        Map<String, Object> body = response.getBody();
        
        assertNotNull(body);
        assertInstanceOf(String.class, body.get("status"));
        assertInstanceOf(String.class, body.get("service"));
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
        assertInstanceOf(Integer.class, body.get("port"));
    }
    
    @Test
    void testHealth_MultipleCallsReturnConsistentData() {
        ResponseEntity<Map<String, Object>> response1 = healthController.health();
        ResponseEntity<Map<String, Object>> response2 = healthController.health();
        
        Map<String, Object> body1 = response1.getBody();
        Map<String, Object> body2 = response2.getBody();
        
        assertNotNull(body1);
        assertNotNull(body2);
        
        // Les données statiques doivent être identiques
        assertEquals(body1.get("status"), body2.get("status"));
        assertEquals(body1.get("service"), body2.get("service"));
        assertEquals(body1.get("port"), body2.get("port"));
        
        // Seul le timestamp peut différer
        assertNotNull(body1.get("timestamp"));
        assertNotNull(body2.get("timestamp"));
    }
    
    /* ===================== PING ENDPOINT ===================== */
    
    @Test
    void testPing_ReturnsOk() {
        ResponseEntity<String> response = healthController.ping();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    void testPing_ReturnsPong() {
        ResponseEntity<String> response = healthController.ping();
        
        assertNotNull(response);
        assertEquals("pong", response.getBody());
    }
    
    @Test
    void testPing_ResponseBodyIsNotNull() {
        ResponseEntity<String> response = healthController.ping();
        
        assertNotNull(response.getBody(), "Response body should not be null");
    }
    
    @Test
    void testPing_ResponseLength() {
        ResponseEntity<String> response = healthController.ping();
        String body = response.getBody();
        
        assertNotNull(body);
        assertEquals(4, body.length(), "Response should be exactly 'pong' (4 characters)");
    }
    
    @Test
    void testPing_MultipleCallsReturnSameResponse() {
        ResponseEntity<String> response1 = healthController.ping();
        ResponseEntity<String> response2 = healthController.ping();
        ResponseEntity<String> response3 = healthController.ping();
        
        assertEquals(response1.getBody(), response2.getBody());
        assertEquals(response2.getBody(), response3.getBody());
        assertEquals("pong", response1.getBody());
        assertEquals("pong", response2.getBody());
        assertEquals("pong", response3.getBody());
    }
    
    @Test
    void testPing_ResponseIsString() {
        ResponseEntity<String> response = healthController.ping();
        
        assertInstanceOf(String.class, response.getBody());
    }
    
    /* ===================== PERFORMANCE ===================== */
    
    @Test
    void testHealth_ResponseTime() {
        long startTime = System.nanoTime();
        
        healthController.health();
        
        long endTime = System.nanoTime();
        long responseTime = (endTime - startTime) / 1_000_000; // Convert to ms
        
        assertTrue(responseTime < 100, 
            "Health check should respond in less than 100ms, actual: " + responseTime + "ms");
    }
    
    @Test
    void testPing_ResponseTime() {
        long startTime = System.nanoTime();
        
        healthController.ping();
        
        long endTime = System.nanoTime();
        long responseTime = (endTime - startTime) / 1_000_000; // Convert to ms
        
        assertTrue(responseTime < 50, 
            "Ping should respond in less than 50ms, actual: " + responseTime + "ms");
    }
    
    @Test
    void testHealth_ConcurrentCalls() {
        // Simuler plusieurs appels
        for (int i = 0; i < 100; i++) {
            ResponseEntity<Map<String, Object>> response = healthController.health();
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
    
    @Test
    void testPing_ConcurrentCalls() {
        // Simuler plusieurs appels
        for (int i = 0; i < 100; i++) {
            ResponseEntity<String> response = healthController.ping();
            assertNotNull(response);
            assertEquals("pong", response.getBody());
        }
    }
}