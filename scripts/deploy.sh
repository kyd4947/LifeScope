#!/usr/bin/env bash
# ============================================================
# LifeScope 배포 스크립트 (vm-1 에서 실행)
#
#   ./scripts/deploy.sh
#
# 전제 조건
#   - /opt/life-scope 에 소스가 Clone 되어 있어야 한다
#   - /opt/life-scope/.env 에 API 키가 있어야 한다
#   - docker / docker compose 가 설치되어 있어야 한다
# ============================================================

set -euo pipefail

APP_DIR="/opt/life-scope"
COMPOSE="docker compose -f ${APP_DIR}/docker-compose.prod.yml"

cd "$APP_DIR"

log() { echo -e "\n\033[1;33m==> $1\033[0m"; }
fail() { echo -e "\033[1;31m[ERROR] $1\033[0m" >&2; exit 1; }

# ---- 사전 점검 ----
log "사전 점검"
[ -f "${APP_DIR}/.env" ] || fail ".env 가 없습니다. API 키를 먼저 등록하세요."
grep -qE '^KOSIS_API_KEY=.+' "${APP_DIR}/.env" || fail ".env 의 KOSIS_API_KEY 가 비어 있습니다."
grep -qE '^MOLIT_API_KEY=.+' "${APP_DIR}/.env" || fail ".env 의 MOLIT_API_KEY 가 비어 있습니다."

# 키 권한은 600 이어야 한다 (읽기 전용 + 소유자만)
chmod 600 "${APP_DIR}/.env"
echo "    .env 권한 : $(stat -c '%a' "${APP_DIR}/.env")"

docker compose version >/dev/null 2>&1 || fail "docker compose 를 사용할 수 없습니다."

# ---- 프론트엔드 빌드 ----
log "프론트엔드 빌드"
if command -v npm >/dev/null 2>&1; then
  cd "${APP_DIR}/frontend"
  npm ci
  npm run build
  cd "$APP_DIR"
  [ -d "${APP_DIR}/frontend/dist" ] || fail "프론트엔드 빌드 결과가 없습니다."
  echo "    dist 생성 : $(find "${APP_DIR}/frontend/dist" -type f | wc -l) 개 파일"
else
  [ -d "${APP_DIR}/frontend/dist" ] || fail "npm 이 없고 frontend/dist/ 도 없습니다. 프론트엔드 산출물을 먼저 업로드하세요."
  echo "    npm 이 없어 건너뜁니다. 사전 업로드된 dist/ 를 사용합니다."
fi

# ---- 백엔드 이미지 빌드 ----
log "백엔드 이미지 빌드"
$COMPOSE build --pull app

# ---- 기동 ----
log "컨테이너 기동"
$COMPOSE up -d

# ---- 헬스체크 ----
log "헬스체크 확인"
echo "    90초간 대기합니다..."
sleep 90

if $COMPOSE ps | grep -q "lifescope-app.*healthy\|lifescope-app.*Up"; then
  echo "    app 상태 : $($COMPOSE ps app | tail -1)"
else
  echo "    app 로그 :"
  $COMPOSE logs --tail=50 app
  fail "앱이 정상 기동하지 않았습니다."
fi

# actuator 는 nginx 에서 막혀 있으므로 컨테이너 내부에서 확인
if docker exec lifescope-app wget -qO- http://127.0.0.1:8080/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
  echo "    health    : UP"
else
  echo "    health    : 확인 필요 (로그 참조)"
  $COMPOSE logs --tail=30 app
fi

log "배포 완료"
echo "    확인 URL : http://131.186.60.251/"
echo "    로그     : cd ${APP_DIR} && ${COMPOSE} logs -f app"
