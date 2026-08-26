-- Full schema for development (H2)

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS stores (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  genre VARCHAR(255),
  address VARCHAR(500),
  prefecture VARCHAR(100),
  phone VARCHAR(50),
  price_range VARCHAR(100),
  image_url VARCHAR(500),
  description VARCHAR(2000),
  category_id BIGINT,
  CONSTRAINT fk_store_category FOREIGN KEY (category_id) REFERENCES categories(id)
);
CREATE INDEX IF NOT EXISTS idx_stores_category ON stores(category_id);
CREATE INDEX IF NOT EXISTS idx_stores_prefecture ON stores(prefecture);

CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  store_id BIGINT,
  customer_name VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(50),
  reserved_at TIMESTAMP,
  notes VARCHAR(2000),
  CONSTRAINT fk_reservation_store FOREIGN KEY (store_id) REFERENCES stores(id)
);
CREATE INDEX IF NOT EXISTS idx_reservations_store ON reservations(store_id);

CREATE TABLE IF NOT EXISTS store_images (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  filename VARCHAR(500),
  filename_webp VARCHAR(500),
  large_filename VARCHAR(500),
  large_filename_webp VARCHAR(500),
  medium_filename VARCHAR(500),
  medium_filename_webp VARCHAR(500),
  thumbnail VARCHAR(500),
  thumbnail_webp VARCHAR(500),
  store_id BIGINT,
  CONSTRAINT fk_storeimage_store FOREIGN KEY (store_id) REFERENCES stores(id)
);
CREATE INDEX IF NOT EXISTS idx_store_images_store ON store_images(store_id);

CREATE TABLE IF NOT EXISTS coupons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  store_id BIGINT,
  code VARCHAR(100),
  title VARCHAR(255),
  description VARCHAR(2000),
  discount_type VARCHAR(20),
  discount_value DOUBLE,
  valid_from TIMESTAMP,
  valid_to TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  image_url VARCHAR(500),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_coupon_store FOREIGN KEY (store_id) REFERENCES stores(id)
);
CREATE INDEX IF NOT EXISTS idx_coupons_store ON coupons(store_id);
CREATE INDEX IF NOT EXISTS idx_coupons_active ON coupons(active);

CREATE TABLE IF NOT EXISTS girls (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  store_id BIGINT,
  name VARCHAR(255),
  age INT,
  bio VARCHAR(2000),
  image_url VARCHAR(500),
  twitter_url VARCHAR(500),
  instagram_url VARCHAR(500),
  line_url VARCHAR(500),
  gallery VARCHAR(2000),
  schedule VARCHAR(2000),
  CONSTRAINT fk_girl_store FOREIGN KEY (store_id) REFERENCES stores(id)
);
CREATE INDEX IF NOT EXISTS idx_girls_store ON girls(store_id);
