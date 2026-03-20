-- Table des méthodes de livraison
CREATE TABLE IF NOT EXISTS shipping_methods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    destination_country VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    estimated_days INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ajouter la colonne shipping_method_id au panier
ALTER TABLE baskets 
ADD COLUMN IF NOT EXISTS shipping_method_id BIGINT;

-- Contrainte de clé étrangère
ALTER TABLE baskets
ADD CONSTRAINT fk_basket_shipping_method 
    FOREIGN KEY (shipping_method_id) 
    REFERENCES shipping_methods(id) 
    ON DELETE SET NULL;

-- Index
CREATE INDEX IF NOT EXISTS idx_shipping_methods_country ON shipping_methods(destination_country);
CREATE INDEX IF NOT EXISTS idx_shipping_methods_enabled ON shipping_methods(enabled);
CREATE INDEX IF NOT EXISTS idx_baskets_shipping_method ON baskets(shipping_method_id);

-- Données de test
INSERT INTO shipping_methods (name, description, destination_country, cost, estimated_days, enabled) VALUES
('Standard France', 'Livraison standard en France métropolitaine', 'France', 5.99, 3, true),
('Express France', 'Livraison express en France métropolitaine', 'France', 12.99, 1, true),
('Standard EU', 'Livraison standard en Union Européenne', 'EU', 9.99, 5, true),
('Express EU', 'Livraison express en Union Européenne', 'EU', 19.99, 2, true),
('Standard International', 'Livraison standard internationale', 'ALL', 15.99, 7, true),
('Retrait en magasin', 'Retrait gratuit en magasin sous 24h', 'France', 0.00, 1, true);