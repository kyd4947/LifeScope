# ============================================================
# LifeScope 백엔드 이미지
#   빌드 : eclipse-temurin:21-jdk  (Gradle 빌드)
#   실행 : eclipse-temurin:21-jre  (JRE 만, 이미지 경량화)
#
# Oracle Cloud VM.Standard.E2.1.Micro 는 x86_64 이므로
# linux/amd64 플랫폼 기준으로 빌드한다.
# ============================================================

# ---------- 1단계 : 빌드 ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# 의존성 선언 파일만 먼저 복사 (레이어 캐시 활용)
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./

# 소스 복사 후 jar 생성
COPY src ./src
RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test

# ---------- 2단계 : 실행 ----------
FROM eclipse-temurin:21-jre

# 한국 시간대 및 UTF-8 (한글 지역명/메시지 처리)
ENV TZ=Asia/Seoul \
    LANG=C.UTF-8 \
    # 1GB VM 에서 다른 컨테이너와 메모리를 나눠 쓰기 위해 힙을 제한한다
    JAVA_OPTS="-Xms192m -Xmx448m -XX:+UseSerialGC -XX:MaxRAMPercentage=75"

# 비 root 사용자 (권한 상승 방지)
RUN groupadd -r lifescope && useradd -r -g lifescope lifescope

WORKDIR /app

# 빌드 산출물만 가져온다
COPY --from=builder /build/build/libs/*.jar app.jar

# ownershipGive root 먼저 만들고 읽기 전용으로 바꾼다
RUN chown -R root:root /app \
    && chmod 644 /app/app.jar

USER lifescope

EXPOSE 8080

# 컨테이너 헬스체크 (actuator 는 내부망에서만 노출되도록 nginx 에서 막는다)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
