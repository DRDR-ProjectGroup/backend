# 프로젝트 소개

배포 주소 : https://drdr.kr

- 테스트 계정 : tester / test!1234
- 관리자 계정 : admin / drdr!1234

Spring Boot 기반의 커뮤니티 서비스 API 서버입니다.<br>
누구나 게시글을 읽을 수 있고, 이메일 인증으로 회원가입을 하여<br>
게시글 작성, 댓글 작성, 좋아요 기능, 쪽지 기능을 사용할 수 있습니다.

<br>

# 프로젝트 목적

Spring Security와 JWT에 대한 이해, 파일업로드에 대한 기술과 S3 사용법,<br>
그리고 Github Actions를 활용한 자동배포 및 AWS 배포하는 방법을 학습 하기 위해 진행한 프로젝트입니다.

<br>

# 기술 스택

### Backend

- Java
- Spring Boot
- Spring Boot Mail
- Spring Data JPA
- Spring Security
- JWT
- Swagger
- Websocket + STOMP

### Database

- MySQL
- Redis

### Infra

- Docker
- Github Actions
- AWS S3
- AWS EC2

<br>

# 프로젝트 구조

```
src
 ├─ domain          : 도메인별 controller/service/repository/entity/dto
 │   ├─ controller  : api 요청 처리
 │   ├─ service     : 비즈니스 로직 처리
 │   ├─ repository  : 데이터 접근
 │   ├─ entity      : 엔티티 관리
 │   └─ dto         : api 요청/응답 객체
 │
 ├─ global          : 공통 설정 (security, redis, exception 등)
 │   └─ websocket   : 웹소켓 + STOMP 설정
 │
 └─ standard        : 유틸파일
```

<br>

# 주요 기능

### 회원

- Naver 이메일로 인증코드를 받아 인증 후 회원가입
- 내가 쓴 글, 내가 쓴 댓글 조회 기능

### 게시글

- 게시글 CRUD 기능
- 이미지나 동영상 업로드 기능

### 댓글

- 댓글 및 대댓글 작성 기능

### 쪽지

- 회원간 쪽지 기능

### 좋아요

- 게시글에 좋아요 기능

### 실시간 채팅

- 웹소켓과 STOMP를 활용한 실시간 채팅 기능

### 관리자

- 카테고리 그룹과 카테고리 관리
- 회원 관리
    - 회원 목록 조회
    - 회원 상태 변경 (차단 기능)

<br>

# API 명세

### Swagger

링크 : https://api.drdr.kr/swagger-ui/index.html

<br>

# 실행 방법

### 1. 환경변수 설정

프로젝트 실행을 위해, 루트경로에 `.env` 파일이 필요합니다.

```
# Database Configuration
DRDR_DB_URL=jdbc:mysql://localhost:3306/doranDB?serverTimezone=Asia/Seoul
DRDR_DB_USERNAME=root
DRDR_DB_PASSWORD=drdr1234

# Redis Configuration
DRDR_REDIS_DB_HOST=localhost
DRDR_REDIS_DB_PORT=6379
DRDR_REDIS_DB_PASSWORD=drdr1234

# JWT Configuration
JWT_SECRET=YOUR_JWT_SECRET
JWT_ACCESS_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=86400

# Mail Configuration
DRDR_MAIL_HOST=smtp.gmail.com
DRDR_MAIL_PORT=587
DRDR_MAIL_USERNAME=YOUR_GMAIL_ADDRESS
DRDR_MAIL_PASSWORD=YOUR_GMAIL_APP_PASSWORD

# Admin Credentials
ADMIN_USERNAME=admin
ADMIN_PASSWORD=drdr!1234

# Tester Credentials
TESTER_USERNAME=tester
TESTER_PASSWORD=test!1234

# Elasticsearch Configuration
DRDR_ES_URL=http://dummy:9200

# AWS S3 Configuration
DRDR_S3_BUCKET=YOUR_S3_BUCKET
DRDR_S3_PREFIX=YOUR_S3_PREFIX
```

### 2. 실행 방법

프로젝트 실행 설정에서 `.env`파일을 포함하여 실행하도록 설정 후 실행해야합니다.

1. IntelliJ 실행
2. task 옵션에서 `Edit Configurations` 클릭
3. `Modify options` 클릭
4. `Environment variables` 클릭
5. `Environment variables` 설정에서 `.env`파일 선택
