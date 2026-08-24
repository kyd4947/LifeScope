# Life Scope (지역별 생활비 분석)
> 전국 시 단위 생활비·월급·주거비 비교 & 계산기

## 개요
> 취업을 위해 타 지역으로 이주하는 청년들이 지역별 생활비 데이터를 기반으로 예상 지출을 산정하고, 보다 합리적인 이주 결정을 내릴 수 있도록 돕는 생활비 분석 서비스입니다.

### 🔧 기술 스택
#### Backend
| 분류 | 기술 |
|---|---|
| Language | Java 21 LTS |
| Framework | Spring Boot 3.3.x |
| Build | Gradle 8.x (Kotlin DSL) |
| DB | PostgreSQL (Flyway 마이그레이션) |
| Cache | Redis (Lettuce) |
| Batch | Spring Batch (Chunk, Retry, 멱등성) |
| 외부 API 내성 | Resilience4j (CircuitBreaker, Retry, Timeout) |
| 문서화 | springdoc-openapi (Swagger UI) |
| 테스트 | JUnit 5, AssertJ, Testcontainers |
| 배포 | Docker, Docker Compose, GitHub Actions, Nginx |
| 인프라 | Oracle Cloud Always Free (ARM 2 OCPU / 12 GB) |

#### Frontend
| 분류 | 기술 |
|---|---|
| 빌드 | Vite |
| 언어 | Vanilla JavaScript (ES Modules) |
| 스타일 | Tailwind CSS |
| 차트 | Chart.js / ApexCharts |
| 배포 | GitHub Pages (`gh-pages` 브랜치) |

### 📊 데이터 소스
| 데이터 | 제공 기관 | API | 갱신 주기 | 지역 단위 |
|---|---|---|---|---|
| 소비자물가지수 (CPI) | 통계청 (KOSIS) | REST (JSON) | 월간 | **시 단위** |
| 평균임금 | 고용노동부 (KOSIS 경유) | REST (JSON) | 연간 | **시/도 단위** |
| 아파트 매매/전세 실거래가 | 국토교통부 | REST (JSON) | 월간 | **시/군/구 단위** |
> **API 키 발급** : [KOSIS 오픈 API](https://kosis.kr/openapi/)   ·   [공공데이터포털](https://data.go.kr/)


### 💡 주요 기능
| 기능 | 설명 | 엔드포인트 |
|---|---|---|
| **지역 검색/자동완성** | 시/도/시군구 계층 구조로 검색 | `GET /api/cities` |
| **다중 지역 비교** | 선택한 지역들의 물가·임금·주거비 나란히 비교 + 차트 | `GET /api/compare?cities=11,26,41` |
| **실수령액 계산기** | 연봉 입력 → 4대보험 + 소득세 공제 후 월 실수령액 | `POST /api/calculator/real-income` |
| **물가보정 환산** | "A도시 월급 X원으로 B도시에서 살려면?" (CPI 비율 적용) | `POST /api/calculator/cost-adjust` |
| **주거비 포함 비교** | 월세/전세 평균가 반영한 실질 구매력 비교 | `POST /api/calculator/housing-adjusted` |
| **자동 데이터 동기화** | 매월 1일 03:00 공공 API → DB 적재 (Spring Batch) | `POST /api/admin/sync` (수동 트리거) |

### 🏠 로컬 개발 환경
- Docker Desktop / Docker Engine + Docker Compose
- JDK 21 (로컬 IDE용)
- Git
