-- ==========================================
-- 1. city (지역 마스터)
-- ==========================================
CREATE TABLE IF NOT EXISTS city (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_code VARCHAR(10),
    level SMALLINT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_city_parent ON city(parent_code);
CREATE INDEX idx_city_level ON city(level);
CREATE INDEX idx_city_active ON city(is_active);

-- ==========================================
-- 2. consumer_price_index (소비자물가지수)
-- ==========================================
CREATE TABLE IF NOT EXISTS consumer_price_index (
    id BIGSERIAL PRIMARY KEY,
    city_code VARCHAR(10) NOT NULL REFERENCES city(code),
    base_year SMALLINT NOT NULL,
    year_month CHAR(6) NOT NULL,
    cpi_value DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (city_code, base_year, year_month)
);

-- 인덱스
CREATE INDEX idx_cpi_city_ym ON consumer_price_index(city_code, year_month DESC);
CREATE INDEX idx_cpi_city ON consumer_price_index(city_code);

-- ==========================================
-- 3. average_wage (평균임금)
-- ==========================================
CREATE TABLE IF NOT EXISTS average_wage (
    id BIGSERIAL PRIMARY KEY,
    city_code VARCHAR(10) NOT NULL REFERENCES city(code),
    year SMALLINT NOT NULL,
    wage_avg BIGINT NOT NULL,
    wage_monthly BIGINT GENERATED ALWAYS AS (wage_avg / 12) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (city_code, year)
);

-- 인덱스
CREATE INDEX idx_wage_city_year ON average_wage(city_code, year DESC);
CREATE INDEX idx_wage_city ON average_wage(city_code);

-- ==========================================
-- 4. housing_price (주거비 실거래가)
-- ==========================================
CREATE TABLE IF NOT EXISTS housing_price (
    id BIGSERIAL PRIMARY KEY,
    city_code VARCHAR(10) NOT NULL REFERENCES city(code),
    trade_type CHAR(1) NOT NULL,
    year_month CHAR(6) NOT NULL,
    avg_price BIGINT NOT NULL,
    deal_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (city_code, trade_type, year_month)
);

-- 인덱스
CREATE INDEX idx_housing_city_type_ym ON housing_price(city_code, trade_type, year_month DESC);
CREATE INDEX idx_housing_city ON housing_price(city_code);