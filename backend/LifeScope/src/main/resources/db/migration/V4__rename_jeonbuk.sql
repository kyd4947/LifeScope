-- V4__rename_jeonbuk.sql
-- 설명: 전라북도 → 전북특별자치도 개명 (2024년 공식 명칭 변경 반영)
-- 이유: KOSIS API 응답의 지역명(C1_NM)과 city.name을 1:1 매칭하기 위함
--       (KOSIS는 '전북특별자치도'로 응답하므로 이름이 다르면 매핑 실패)
-- 영향: code(45000)·계층 구조 변경 없음, 이름만 변경

UPDATE city SET name = '전북특별자치도' WHERE code = '45000' AND name = '전라북도';
