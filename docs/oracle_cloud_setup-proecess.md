# Oracle Cloud 인프라 구축 가이드 (Life Scope)

> **작성일**: 2026-08-26  
> **리전**: 도쿄 (ap-tokyo-1) · **플랜**: Always Free  
> **구성**: VM 2대 (VM.Standard.E2.1.Micro) — vm-1: 앱+Redis / vm-2: PostgreSQL

---

## 1. 계정 가입

| 단계 | 내용 |
|---|---|
| 1 | https://www.oracle.com/cloud/free/ → "Start for free" |
| 2 | 이메일 인증 → 휴대폰 인증 |
| 3 | 카드 등록 (**해외결제 가능한 체크/신용카드**, 1달러 승인 후 자동 취소) |
| 4 | 홈 리전 선택 — **한 번 정하면 변경 불가** (본 프로젝트: 도쿄) |
| 5 | "Always Free" 유지 (PAYG 업그레이드 권유는 거절) |

### Always Free 한도 (2026.8 기준)

| 리소스 | 한도 |
|---|---|
| ARM (Ampere A1 Flex) | 2 OCPU / 12 GB (2026.6월에 4/24 → 2/12 축소) |
| x86 Micro (E2.1.Micro) | **2대** × 1 OCPU / 1 GB |
| 블록 스토리지 | 200 GB |
| 아웃바운드 | 10 TB/월 |

> ⚠️ Always Free 리소스는 **홈 리전에서만** 생성 가능.  
> ⚠️ 7일간 CPU/네트워크/메모리 20% 미만이면 유휴 인스턴스 회수 대상이 될 수 있음.

---

## 2. 인스턴스 생성

### 2-1. 셰이프 선택

- `Compute → Instances → Create instance`
- Image: **Canonical Ubuntu 22.04**
- Shape: `Change shape` → **Specialty and previous generation** → `VM.Standard.E2.1.Micro` (Always Free-eligible)
  - Micro는 **고정형**이라 OCPU/메모리 수정 불가 (1 OCPU / 1 GB 고정)

### 2-2. 네트워킹

| 항목 | 값 |
|---|---|
| Primary network | Create new virtual cloud network → `lifescope-vcn` |
| Subnet | Create new **public** subnet → `lifescope-subnet` (CIDR 10.0.0.0/24) |
| **Public IPv4 address assignment** | **ON** ✅ (공용 IP 필수 — 끄면 외부 접속 불가) |

> 💡 인스턴스 생성 화면에서 VCN을 새로 만들면 Public IPv4 토글이 비활성화되는 경우가 있음.  
> 그럴 땐 `Networking → Start VCN Wizard → Create VCN with Internet Connectivity`로  
> VCN을 먼저 만들고, 인스턴스 생성 시 "Select existing"으로 선택하는 것이 안전.

### 2-3. SSH 키 — ⚠️ 가장 많이 실수하는 구간

**방식 A: 콘솔에서 Generate (본 프로젝트 채택)**

1. `Add SSH keys` → `Generate a key pair for me` → **딱 한 번만 클릭**
2. `Download Private Key` → 다운로드 즉시 파일 확인
3. `Download Public Key` → 마찬가지 확인
4. **Generate를 다시 누르면 이전 키는 무효화됨** (키 어긋남의 주원인)

**방식 B: 로컬에서 직접 생성 (더 안전, 추천)**

```bash
ssh-keygen -t rsa -b 2048 -f ~/.ssh/lifescope_key -N ""
chmod 600 ~/.ssh/lifescope_key
cat ~/.ssh/lifescope_key.pub   # 출력 → 콘솔 "Paste a public key"에 붙여넣기
```

### 키 관련 절대 규칙

- **개인키(`.key`)**: 채팅/콘솔/깃 저장소 **어디에도 올리지 않는다** (유출 시 서버 해킹)
- **공개키(`.pub`)**: 자유롭게 서버에 등록 가능
- 키 파일은 **깃 저장소 밖**에 보관 (예: `D:\ssh\`, `~/.ssh/`)
- `.gitignore`에 `*.key`, `*.pem` 추가 권장

### 2-4. 두 번째 인스턴스

- **같은 VCN/서브넷 재사용** (Select existing)
- Public IPv4: Yes
- SSH keys: `Upload a public key file (.pub)` → **1번과 동일한 `.pub` 파일 업로드**
- ⚠️ `Generate a key pair for me`를 **다시 누르지 않는다** (키 어긋남 방지)

### 최종 생성 결과

| 인스턴스 | 공용 IP | 내부 IP | 역할 |
|---|---|---|---|
| lifescope-vm-1 | 131.186.60.251 | 10.0.0.154 | Spring Boot + Redis |
| lifescope-vm-2 | 158.179.181.70 | 10.0.0.149 | PostgreSQL |

---

## 3. SSH 접속

```bash
# 키가 있는 디렉토리에서 (예: D:\ssh)
cd /d/ssh
chmod 600 ssh-key-2026-08-26.key

# 1번 VM
ssh -i ssh-key-2026-08-26.key ubuntu@131.186.60.251

# 2번 VM (같은 키)
ssh -i ssh-key-2026-08-26.key ubuntu@158.179.181.70
```

- 첫 접속 시 fingerprint 질문 → `yes`
- `ubuntu@...` 프롬프트가 뜨면 성공 (Ubuntu 이미지 기본 사용자는 `ubuntu`)

---

## 4. 보안 리스트 (포트 개방)

**중요**: 보안 리스트는 **서브넷 단위**로 적용되므로, 같은 서브넷의 모든 VM에 공통 적용됨.  
→ vm-2에 별도로 추가할 필요 없음.

```
Networking → Virtual cloud networks → lifescope-vcn
→ Security Lists → Default Security List → Add Ingress Rules
```

추가한 규칙 (Stateless 체크 안 함):

| Source CIDR | IP Protocol | Destination Port | 용도 |
|---|---|---|---|
| 0.0.0.0/0 | TCP | 22 | SSH (기본 제공) |
| 0.0.0.0/0 | TCP | 80 | HTTP |
| 0.0.0.0/0 | TCP | 443 | HTTPS |
| 0.0.0.0/0 | TCP | 8080 | Spring Boot 직접 접근 (개발용) |

> ICMP 규칙 2개(3,4 / 3)는 오라클 기본 규칙 — 삭제하지 말 것.

---

## 5. 서버 초기 세팅 (양쪽 VM 모두)

SSH 접속 후 실행:

```bash
# 1. 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# 2. 필수 패키지 설치
sudo apt install -y docker.io docker-compose-plugin nginx certbot python3-certbot-nginx git

# 3. Docker 그룹 권한 (실행 후 재접속 필요)
sudo usermod -aG docker ubuntu

# 4. 프로젝트 디렉토리
sudo mkdir -p /opt/life-scope
sudo chown ubuntu:ubuntu /opt/life-scope
```

재접속 후 검증:

```bash
exit
ssh -i ssh-key-2026-08-26.key ubuntu@<공용IP>
docker --version          # Docker version 24.x 출력 확인
docker compose version    # Docker Compose v2.x 출력 확인
```

---

## 6. 트러블슈팅 로그 (실제 겪은 문제들)

### 6-1. ARM 용량 부족
```
API Error: Out of capacity for shape VM.Standard.A1.Flex in availability domain AD-1
```
- **원인**: 무료 ARM 인스턴스는 수요가 많아 자주 품절
- **해결**: `VM.Standard.E2.1.Micro` 2대로 폴백 (ARM은 나중에 주기적 재시도)

### 6-2. Public IPv4 토글 비활성화
- **원인**: 인스턴스 생성 화면에서 VCN을 새로 만들 때 네트워크 구성이 확정 전 상태로 남음
- **해결**: VCN Wizard로 네트워크 선생성 후 기존 VCN 선택, 또는 재시도

### 6-3. Permission denied (publickey)
```
ubuntu@IP: Permission denied (publickey)
```
- **원인**: `Generate a key pair for me`를 여러 번 클릭해 다운로드한 키와 인스턴스에 등록된 키가 불일치. 프라이빗 키는 복구 불가
- **해결**: 인스턴스 삭제 후 재생성, 키는 **한 번만 Generate**하고 두 인스턴스에 같은 공개키 등록

### 6-4. UNPROTECTED PRIVATE KEY FILE
- **원인**: 키 파일 권한이 0777 (Windows 기본)
- **해결**: `chmod 600 키파일` (Git Bash) 또는 `icacls`로 Windows ACL 제한

### 6-5. chmod 명령어를 PowerShell에서 실행
- **원인**: `chmod`는 유닉스 명령어
- **해결**: **Git Bash** 사용, 또는 PowerShell에서는 `icacls` 사용

---

## 7. 다음 단계 (예정)

- [ ] 로컬 Spring Boot 프로젝트 생성 (`start.spring.io`)
- [ ] Docker Compose로 로컬 스택 구성 (PostgreSQL + Redis + App)
- [ ] vm-1: 앱 + Redis 배포 / vm-2: PostgreSQL 배포
- [ ] Nginx 리버스 프록시 + Let's Encrypt SSL (도메인 연결 후)
- [ ] GitHub Actions CI/CD 파이프라인 구성
- [ ] GitHub Pages 프론트엔드 배포

---

## 부록: 자주 쓰는 명령어

```bash
# SSH 접속
ssh -i ssh-key-2026-08-26.key ubuntu@131.186.60.251   # vm-1
ssh -i ssh-key-2026-08-26.key ubuntu@158.179.181.70   # vm-2

# 공개키 내용 확인 (개인키에서 재추출)
ssh-keygen -y -f ssh-key-2026-08-26.key

# 서버 공용 IP 확인 (VM 내부에서)
curl -s ifconfig.me
```