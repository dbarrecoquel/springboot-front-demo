-- Ajouter la colonne active à la table products
ALTER TABLE products ADD COLUMN active BOOLEAN DEFAULT true NOT NULL;

-- Créer un index
CREATE INDEX idx_products_active ON products(active);

-- Verifier la colonne
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'products' AND column_name = 'active';