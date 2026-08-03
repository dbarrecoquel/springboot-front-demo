package com.example.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "com.example.user.model"
})
public class TestApplication {
    // Classe vide servant uniquement de point d'ancrage pour les tests du module shopping
}