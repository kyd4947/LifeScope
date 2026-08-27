> **작성일**: 2025-08-25  
> **프로젝트**: Life Scope — 전국 시 단위 생활비/월급/주거비 비교 & 계산기

---

## 1. 백엔드 핵심 스택

### Java 21 LTS
| 선정 이유 | 비고 |
|---|---|
| **LTS 버전** | 2026년 9월까지 프리미어 지원, 2031년까지 연장 지원 |
| **가상 스레드 (Virtual Threads)** | Spring Boot 4.1+에서 네이티브 지원. 고동시성 API 서버에 최적 (요청당 스레드 1:1 매핑 가능) |
| **패턴 매칭, Record, Sealed Classes** | 도메인 모델링(Domain Modeling) 가독성/안전성 향상 |
| **성능 향상** | JIT 컴파일러, GC(ZGC/Generational ZGC) 개선으로 지연시간 감소 |

> **대안 비교**: Java 17 LTS도 안정적이나, 가상 스레드 네이티브 지원이 21부터라 신규 프로젝트는 21 권장.

---

### Spring Boot 4.1.x
| 선정 이유 | 비고 |
|---|---|
| **Java 17+ 베이스라인** | Java 21 기능 완전 활용 가능 (가상 스레드, 패턴 매칭 등) |
| **GraalVM 네이티브 이미지 지원** | 향후 컨테이너 이미지 크기/시작시간 최적화 시 마이그레이션 용이 |
| **Observability (Actuator, Micrometer)** | 운영 메트릭/헬스체크/트레이싱 표준 내장 |
| **Spring Batch, Security, Data, Web** | 필요한 모듈 생태계 완비 |
| **자동 설정(Auto-configuration)** | 보일러플레이트 최소화, 컨벤션 오버 컨피규레이션 |

> **대안 비교**: Node.js(Express/NestJS), Go(Gin)도 고려했으나, **배치 처리(Spring Batch)**, **복잡한 트랜잭션/ORM**, **한국 공공 API 연동(자바 기반 SDK 다수)** 특성상 Spring 생태계가 생산성 최고.

---

### Gradle 8.x (Kotlin DSL)
| 선정 이유 | 비고 |
|---|---|
| **타입 세이프 빌드 스크립트** | Groovy DSL 대비 IDE 지원(자동완성, 리팩토링, 에러 감지) 우수 |
| **빌드 캐시/증분 빌드** | 멀티모듈 확장 시 성능 이점 |
| **버전 카탈로그(Version Catalog)** | 의존성 버전 중앙 관리, 일관성 보장 |
| **Kotlin 언어 익숙도** | 타입 안전 언어라 빌드 로직도 타입 검증 가능 |

> **대안 비교**: Maven은 XML 장황, Groovy DSL은 런타임 에러 가능성. Kotlin DSL이 현대적 표준.

---

### Spring Data JPA (Hibernate)
| 선정 이유 | 비고 |
|---|---|
| **리포지토리 추상화** | `JpaRepository` 인터페이스만으로 CRUD/페이징/정렬 해결 |
| **쿼리 메서드/JPQL/네이티브 쿼리** | 단순~복잡 쿼리 모두 커버 |
| **변경 감지(Dirty Checking)** | 트랜잭션 내 엔티티 수정 시 자동 UPDATE |
| **Flyway와 자연스러운 통합** | 스키마 버전 관리 + 엔티티 매핑 동기화 |

> **대안 비교**: MyBatis는 SQL 직접 제어 가능하지만 보일러플레이트 많음. QueryDSL/jOOQ는 **복잡한 동적 쿼리 필요 시(선택적 추가)** 도입 예정.

---

### Spring Data Redis (Lettuce)
| 선정 이유 | 비고 |
|---|---|
| **비동기/논블로킹** | Netty 기반, 가상 스레드 환경과 궁합 좋음 |
| **커넥션 풀링 내장** | 별도 설정 없이 고성능 연결 관리 |
| **RedisTemplate + 직렬화** | 객체 직렬화/역직렬화 편의 제공 |
| **분산 락, 캐시, 세션** | 배치 중복 실행 방지 락, API 응답 캐시, 세션 저장 모두 단일 인프라로 해결 |

---

### Spring Batch
| 선정 이유 | 비고 |
|---|---|
| **Chunk 기반 처리** | 대용량 데이터(전국 3,500개 지역 × 월별 데이터) 메모리 안전 적재 |
| **재시도/스킵/재시작** | 외부 API 장애 시 자동 재시도, 실패 레코드 스킵, 중단 지점부터 재개 |
| **멱등성 보장** | `ItemWriter`에서 `ON CONFLICT DO UPDATE`(UPSERT) 패턴으로 재실행 안전 |
| **Job/Step/Listener 구조** | 모니터링/알림/로깅 확장 용이 |
| **Spring 생태계 네이티브** | 별도 스케줄러(Quartz 등) 없이 `@Scheduled` + `JobLauncher`로 충분 |

> **대안 비교**: 단순 `@Scheduled` + 서비스 로직으로 구현 시 재시도/멱등성/청크 처리 직접 구현 필요 → 보일러플레이트 과다. Batch 프레임워크가 표준 패턴 제공.

---

### Resilience4j
| 선정 이유 | 비고 |
|---|---|
| **경량(Zero Dependency)** | Hystrix 대비 의존성 없음, 함수형 스타일 |
| **Circuit Breaker, Retry, Timeout, Bulkhead, RateLimiter** | 공공 API 호출에 필요한 모든 패턴 내장 |
| **Spring Boot 3.x 공식 지원** | `resilience4j-spring-boot3` 스타터로 자동 설정 |
| **액추에이터 연동** | 서킷 브레이커 상태 메트릭 자동 노출 |

> **적용 지점**: KOSIS/국토부 API 클라이언트 메서드에 `@CircuitBreaker`, `@Retry`, `@TimeLimiter` 어노테이션 선언적 적용.

---

### Flyway
| 선정 이유 | 비고 |
|---|---|
| **버전 관리형 마이그레이션** | `V1__init.sql`, `V2__data.sql` 순차 적용, 체크섬 검증으로 무결성 보장 |
| **스키마 이력 테이블** | `flyway_schema_history`로 적용 내역 추적 |
| **Spring Boot 통합** | `spring.flyway.enabled=true`만으로 자동 실행 |
| **롤백 불가 정책** | 운영 안전성(롤백 대신 보정 마이그레이션 `V4__fix...` 작성) |

> **대안 비교**: Liquibase는 XML/YAML 기반이라 SQL 네이티브 선호 팀에선 Flyway가 더 직관적.

---

### springdoc-openapi (Swagger UI)
| 선정 이유 | 비고 |
|---|---|
| **코드에서 문서 자동 생성** | 컨트롤러/DTO 어노테이션(`@Operation`, `@Schema`)만으로 OpenAPI 3.0 스펙 생성 |
| **Swagger UI 내장** | `/swagger-ui.html`로 즉시 테스트 가능 |
| **Spring Boot 3.x 완벽 지원** | `springdoc-openapi-starter-webmvc-ui` 원라인 의존성 |
| **프론트엔드 협업** | 타입스크립트 클라이언트 생성(`openapi-generator`) 가능 |

---

### Testcontainers
| 선정 이유 | 비고 |
|---|---|
| **실제 DB/Redis 컨테이너로 테스트** | H2/임베디드 DB와 달리 프로덕션과 동일한 PostgreSQL, Redis 동작 검증 |
| **JUnit 5 통합** | `@Testcontainers`, `@Container` 어노테이션으로 생명주기 자동 관리 |
| **CI/CD에서 동일 환경** | 로컬/깃허브 액션/운영 모두 동일 컨테이너 이미지 사용 → "내 컴퓨터에선 되는데" 방지 |

> **대안 비교**: H2는 PostgreSQL 전용 기능(파티셔닝, JSONB, 인덱스 힌트 등) 검증 불가. Testcontainers가 실용적.

---

## 2. 데이터 파이프라인/외부 연동

### KOSIS OpenAPI / 국토교통부 공공데이터포털 API
| 선정 이유 | 비고 |
|---|---|
| **공식 정부 통계** | 신뢰도 최고, 법적 근거 있음 |
| **무료(인증키만 발급)** | 비용 0원, 일일 호출 제한만 관리하면 됨 |
| **REST/JSON 표준** | 별도 SDK 없이 `RestClient`/`WebClient`로 직연동 가능 |
| **갱신 주기 명확** | 월간/연간 스케줄 배치로 자동화 적합 |

> **주의**: KOSIS는 시도/시 단위 CPI 제공, 임금은 **시도 단위만** 제공 → 시군구 단위 임금은 근사치/보간 불가. 설계 시 단위 불일치 명시 필요.

---

## 3. 인프라/배포

### Oracle Cloud Always Free (ARM Ampere A1)
| 선정 이유 | 비고 |
|---|---|
| **ARM 2 OCPU / 12 GB RAM 무료** | Spring Boot + PostgreSQL + Redis 24/7 구동 충분 (Render 무료 512MB 대비 24배) |
| **200 GB 블록 스토리지** | DB 데이터, 로그, 백업 넉넉 |
| **10 TB/월 아웃바운드** | 트래픽 비용 걱정 없음 |
| **상시 가동(No Spin-down)** | Render 무료처럼 15분 유휴 시 절전 없음 → 배치 스케줄러 정상 동작 |
| **자율 DB(Autonomous DB) 무료** | 관리형 PostgreSQL 대안으로 운영 부담 최소화 가능 |

> **리스크**: 2026.6월 ARM 할당량 4→2 OCPU 축소, 유휴 인스턴스 회수 정책(7일 20% 미만) → 배치 스케줄러가 월 1회 돌며 CPU 사용하므로 회수 회피에 도움.

> **대안 비교**: 
> - Render Free: 512MB/0.1CPU, 750h/월, 30일 후 DB 만료, 절전 → **배치/상시 서비스 불가**
> - Fly.io/Koyeb: 무료 티어 있지만 절전/리소스 제한 유사
> - 자택 서버: 전기요금/공인IP/정전 리스크

---

### Docker / Docker Compose
| 선정 이유 | 비고 |
|---|---|
| **환경 일관성** | 로컬/스테이징/운영 동일 이미지 실행 |
| **멀티 컨테이너 오케스트레이션** | `docker-compose.yml` 하나로 PG + Redis + App 기동 |
| **멀티스테이지 빌드** | 빌드 환경(Gradle/JDK) → 런타임(JRE) 분리 → 이미지 경량화(~100MB) |
| **CI/CD 연동** | GitHub Actions에서 `docker build/push` 표준 지원 |

---

### Nginx (리버스 프록시 + SSL 종료)
| 선정 이유 | 비고 |
|---|---|
| **경량 고성능** | 이벤트 드리븐, C10K 문제 해결 표준 |
| **SSL 종료(Termination)** | Let's Encrypt(Certbot) 자동 갱신 + HTTP→HTTPS 리다이렉트 |
| **정적 파일 서빙** | 프론트엔드 빌드 산출물(`dist/`) 직접 서빙 가능(향후 통합 배포 시) |
| **로드밸런싱/레이트리밋** | 향후 스케일아웃 시 업스트림 분산 용이 |

---

### GitHub Actions (CI/CD)
| 선정 이유 | 비고 |
|---|---|
| **퍼블릭 레포 무료** | 러너 시간 무제한(퍼블릭), 프라이빗도 월 2,000분 무료 |
| **워크플로우 코드화** | YAML로 빌드/테스트/배포 파이프라인 버전 관리 |
| **시크릿 관리** | SSH 키, API 키 안전 주입 |
| **다양한 액션 생태계** | `actions/checkout`, `setup-java`, `docker/build-push`, `ssh-agent`, `gh-pages` 등 즉시 사용 |

---

### GitHub Pages (프론트엔드 호스팅)
| 선정 이유 | 비고 |
|---|---|
| **완전 무료 + 무제한 트래픽** | 정적 사이트 호스팅 최적 |
| **커스텀 도메인 지원** | `lifescope.yourdomain.com` 연결 가능 |
| **자동 HTTPS** | 인증서 관리 불필요 |
| **`gh-pages` 브랜치 푸시만으로 배포** | 별도 CI/CD 서버 불필요, `peaceiris/actions-gh-pages` 원라인 |

---

## 4. 프론트엔드 (예정)

### Vite + Vanilla JavaScript (ES Modules)
| 선정 이유 | 비고 |
|---|---|
| **번들러 불필요(개발 시)** | 네이티브 ESM 핫 리로드, 즉각적 피드백 |
| **프레임워크 러닝커브 0** | React/Vue/Svelte 학습 없이 바로 구현 |
| **경량 번들** | 런타임 오버헤드 없음, gzip 10KB 내외 |
| **참고 사이트 모방 용이** | 순수 HTML/CSS/JS로 어떤 디자인이든 픽셀 단위 구현 가능 |
| **GitHub Pages 정적 배포 최적** | `dist/` 폴더 그대로 `gh-pages` 브랜치 푸시 |

> **대안 비교**: React는 컴포넌트 재사용/상태관리 강점이지만, 이 프로젝트는 **페이지 4개(Home/Compare/Calculator/About) + 차트/폼 위주**라 Vanilla로 충분. 차트 라이브러리만 ESM 임포트.

---

### Tailwind CSS
| 선정 이유 | 비고 |
|---|---|
| **유틸리티 퍼스트** | 커스텀 CSS 작성 최소화, HTML에서 스타일 완결 |
| **JIT 컴파일러** | 사용한 클래스만 번들링 → 프로덕션 CSS 10KB 미만 |
| **반응형/다크모드/테마** | 내장 유틸리티로 즉시 적용 |
| **참고 사이트 디자인 시스템 매핑** | 색상/간격/타이포그래피 토큰화 → `tailwind.config.js`로 중앙 관리 |

---

### Chart.js / ApexCharts
| 선정 이유 | 비고 |
|---|---|
| **ESM 지원** | `import Chart from 'chart.js/auto'` 바로 사용 |
| **반응형 캔버스** | 모바일/데스크톱 자동 리사이즈 |
| **애니메이션/툴팁/레전드** | 비교 차트(막대/레이더/라인) UX 필수 요소 내장 |
| **타입스크립트 타입 정의** | 향후 TS 마이그레이션 시 타입 안전 |

---

## 5. 개발/운영 도구

### Lombok
| 선정 이유 | 비고 |
|---|---|
| **보일러플레이트 제거** | `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j` |
| **컴파일 타임 처리** | 런타임 오버헤드 0, 바이트코드 조작으로 안전 |

---

### AssertJ
| 선정 이유 | 비고 |
|---|---|
| **플루언트 어설션** | `assertThat(list).hasSize(3).first().isEqualTo(expected)` 가독성 최고 |
| **풍부한 에러 메시지** | 실패 시 차이점 시각적 표시 |

---

## 6. 선택적 추가 스택 (향후 필요 시)

| 스택 | 도입 트리거 | 비고 |
|---|---|---|
| **QueryDSL / jOOQ** | 동적 쿼리(다중 필터/정렬/조인) 복잡해질 때 | 타입 세이프 쿼리, SQL 인젝션 방지 |
| **Spring Security + JWT** | 관리자 페이지, 사용자 인증 필요 시 | Stateless REST API 인증 표준 |
| **Actuator + Micrometer + Prometheus + Grafana** | 운영 메트릭/알림/대시보드 필요 시 | JVM/HTTP/비즈니스 메트릭 수집 |
| **Spring Cloud OpenFeign** | 외부 API 클라이언트 코드 더 선언적으로 쓰고 싶을 때 | 인터페이스만 정의, 구현체 자동 생성 |

---

## 7. 요약: 선택 기준 매트릭스

| 기준 | 가중치 | 선택된 스택 | 이유 요약 |
|---|---|---|---|
| **생산성(보일러플레이트 최소)** | 높음 | Spring Boot, JPA, Lombok, Flyway, Spring Batch | 표준 프레임워크로 비즈니스 로직 집중 |
| **운영 안정성(장애 격리/재시도)** | 높음 | Resilience4j, Spring Batch, Redis, Testcontainers | 외부 API 장애/데이터 정합성 보장 |
| **비용(무료 티어 최대 활용)** | 높음 | Oracle Cloud, GitHub Pages, GitHub Actions | 월 0원 운영 달성 |
| **성능(동시성/지연시간)** | 중간 | Java 21 가상 스레드, Lettuce, Nginx | 소규모 VM에서 최대 처리량 |
| **확장성(마이크로서비스 전환)** | 낮음 | 모듈러 모놀리스 구조, Docker/K8s 호환 | 현재는 모놀리스, 향후 분해 가능 |
| **학습 곡선/유지보수** | 중간 | Vanilla JS, Kotlin DSL, 선언적 설정 | 팀 규모 1명 기준 인지 부하 최소화 |

---
