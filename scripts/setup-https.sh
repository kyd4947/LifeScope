#!/usr/bin/env bash
# ============================================================
# LifeScope 백엔드 HTTPS 설정 (vm-1 에서 실행)
#
# 사용법:
#   sudo ./scripts/setup-https.sh ls-api.duckdns.org 이메일주소
#
# 전제 조건:
#   - docker-compose.prod.yml 의 nginx 포트가 127.0.0.1:8080 로 바뀌어 있어야 한다.
#   - Oracle Cloud 보안 리스트에 TCP 80 과 TCP 443 이 허용되어 있어야 한다.
#   - 도메인 DNS 가 vm-1 공인 IP 로 연결되어 있어야 한다.
# ============================================================

set -euo pipefail

DOMAIN="${1:-ls-api.duckdns.org}"
EMAIL="${2:?이메일 주소를 두 번째 인자로 입력하세요}"
SITE_FILE="/etc/nginx/sites-available/lifescope-api"
WEBROOT="/var/www/html"
CERT_DIR="/etc/letsencrypt/live/${DOMAIN}"

log() { echo -e "\n\033[1;33m==> $1\033[0m"; }
fail() { echo -e "\033[1;31m[ERROR] $1\033[0m" >&2; exit 1; }

[ "$(id -u)" -eq 0 ] || fail "root 권한으로 실행해야 합니다. sudo 를 사용하십시오."

log "패키지 설치"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y nginx certbot

if ss -ltn | awk '$4 ~ /:80$/ { found = 1 } END { exit !found }'; then
    if systemctl is-active --quiet nginx; then
        log "기존 호스트 nginx 중지"
        systemctl stop nginx
    else
        fail "80번 포트를 이미 사용 중입니다. docker nginx 를 127.0.0.1:8080 으로 옮긴 뒤 다시 실행하십시오."
    fi
fi

log "인증서용 웹 루트 준비"
mkdir -p "${WEBROOT}/.well-known/acme-challenge"
chmod -R 755 "${WEBROOT}/.well-known"

log "HTTP 초기 설정 작성"
if [ -f "$SITE_FILE" ]; then
    cp -a "$SITE_FILE" "${SITE_FILE}.bak.$(date +%Y%m%d%H%M%S)"
fi

cat > "$SITE_FILE" <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN};

    location ^~ /.well-known/acme-challenge/ {
        root ${WEBROOT};
        default_type text/plain;
    }

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout 300s;
    }
}
EOF

rm -f /etc/nginx/sites-enabled/default
ln -sfn "$SITE_FILE" /etc/nginx/sites-enabled/lifescope-api
systemctl enable nginx
nginx -t
systemctl restart nginx

log "SSL 인증서 발급"
if [ ! -f "${CERT_DIR}/fullchain.pem" ]; then
    certbot certonly \
        --webroot \
        -w "$WEBROOT" \
        -d "$DOMAIN" \
        --agree-tos \
        -m "$EMAIL" \
        --non-interactive \
        --keep-until-expiring
else
    echo "    기존 인증서를 사용합니다."
fi

log "HTTPS 설정 적용"
cat > "$SITE_FILE" <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN};

    location ^~ /.well-known/acme-challenge/ {
        root ${WEBROOT};
        default_type text/plain;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name ${DOMAIN};

    ssl_certificate ${CERT_DIR}/fullchain.pem;
    ssl_certificate_key ${CERT_DIR}/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;

    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Content-Type-Options "nosniff" always;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_connect_timeout 10s;
        proxy_send_timeout 60s;
        proxy_read_timeout 300s;
    }
}
EOF

nginx -t
systemctl reload nginx
systemctl enable --now certbot.timer 2>/dev/null || true

log "HTTPS 확인"
curl -fsS --max-time 20 "https://${DOMAIN}/api/cities" >/dev/null

echo "    API 주소 : https://${DOMAIN}/api"
echo "    인증서   : ${CERT_DIR}"
echo "    갱신 확인 : sudo certbot renew --dry-run"
