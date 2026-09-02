-- V3__char_to_varchar.sql
-- 설명: CHAR → VARCHAR 타입 변경
-- 원인: Hibernate 7의 String 필드 검증은 Types#VARCHAR 기대
--       PostgreSQL의 CHAR(bpchar)과 불일치하여 ddl-auto: validate 실패
--       (columnDefinition으로는 해결 불가 - JDBC 타입 자체가 다름)
-- 데이터 손실 없음: PostgreSQL이 CHAR→VARCHAR 자동 변환 (USING 절 불필요)
-- 대상: year_month 2곳(소비자물가지수, 주거비), trade_type 1곳(주거비)

-- 소비자물가지수: 연월 CHAR(6) → VARCHAR(6)
ALTER TABLE consumer_price_index ALTER COLUMN year_month TYPE VARCHAR(6);

-- 주거비: 거래유형 CHAR(1) → VARCHAR(1)
ALTER TABLE housing_price ALTER COLUMN trade_type TYPE VARCHAR(1);

-- 주거비: 연월 CHAR(6) → VARCHAR(6)
ALTER TABLE housing_price ALTER COLUMN year_month TYPE VARCHAR(6);
