# Backend API 문서

> 작성 기준: 업로드된 최신 `src(1).zip` 소스 코드  
> 대상 서버: Spring Boot 기반 로컬 인증 API 서버  
> 기본 프로필: `local`

---

## 0. 이번 수정 반영 요약

이전 API 문서 대비 인증 API 경로는 그대로이며, **로그인 / 토큰 재발급 응답 구조가 변경**되었습니다.

### 변경 전

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "accessTokenExpiresAt": "...",
  "refreshTokenExpiresAt": "..."
}
```

### 변경 후

```json
{
  "token": {
    "accessToken": "...",
    "refreshToken": "...",
    "accessTokenExpiresAt": "...",
    "refreshTokenExpiresAt": "..."
  },
  "user": {
    "userId": 1,
    "email": "test@example.com",
    "nickname": "jch",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

### 변경 영향

| API | 변경 여부 |
|---|---|
| `POST /api/auth/signup` | 변경 없음 |
| `POST /api/auth/login` | 응답 구조 변경 |
| `POST /api/auth/refresh` | 응답 구조 변경 |
| `POST /api/auth/logout` | 변경 없음 |
| `GET /health` | 변경 없음 |

---

# 1. 개요

이 서버는 회원가입 및 로그인 기능을 위한 인증 API를 제공합니다.

## 제공 기능

- 회원가입
- 로그인
- Access Token / Refresh Token 발급
- Refresh Token 재발급 및 회전
- 로그아웃
- 헬스 체크

## 기본 정보

| 항목 | 값 |
|---|---|
| Base URL | `http://localhost:8080` |
| 기본 Content-Type | `application/json` |
| Access Token 만료 시간 | `900초` = 15분 |
| Refresh Token 만료 시간 | `1,209,600초` = 14일 |
| 인증 방식 | `Authorization: Bearer {accessToken}` |
| Refresh Token 저장 방식 | 원문 미저장, SHA-256 해시 저장 |

---

# 2. 인증 정책

## 2.1 인증 없이 호출 가능한 API

| Method | Path |
|---|---|
| `POST` | `/api/auth/signup` |
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/refresh` |
| `POST` | `/api/auth/logout` |
| `GET` | `/health` |
| `GET` | `/actuator/health` |

## 2.2 보호 API

현재 업로드된 소스에는 별도의 보호 API 엔드포인트가 구현되어 있지 않습니다.  
다만 보안 설정상 위의 공개 API를 제외한 나머지 요청은 인증이 필요합니다.

보호 API 요청 예시:

```http
Authorization: Bearer {accessToken}
```

## 2.3 공개 API에 Authorization 헤더를 함께 보낼 때의 주의점

공개 API라도 `Authorization` 헤더를 보냈는데 형식이 잘못되었거나 토큰이 유효하지 않으면 `401 AUTH_INVALID_ACCESS_TOKEN`이 반환될 수 있습니다.  
인증이 필요 없는 API를 호출할 때는 불필요한 `Authorization` 헤더를 보내지 않는 편이 안전합니다.

## 2.4 Access Token

- JWT 형식
- 토큰 타입: `ACCESS`
- 포함 Claim:
  - `sub`: 사용자 ID
  - `email`
  - `role`
  - `tokenType`
  - `iss`
  - `iat`
  - `exp`
  - `jti`

## 2.5 Refresh Token

- JWT가 아닌 랜덤 문자열
- 서버 DB에는 원문이 아닌 해시값만 저장
- 재발급 시 기존 Refresh Token은 폐기되고 새 Refresh Token이 발급됨
- 이미 폐기된 Refresh Token을 재사용하면 해당 사용자의 남아 있는 활성 Refresh Token도 전부 폐기됨

---

# 3. 공통 규칙

## 3.1 이메일 정규화

회원가입 및 로그인 시 이메일은 내부적으로 아래와 같이 정규화됩니다.

- 앞뒤 공백 제거
- 소문자 변환

예:

```text
" Test@Example.com " → "test@example.com"
```

## 3.2 닉네임 정규화

회원가입 시 닉네임은 앞뒤 공백이 제거됩니다.

## 3.3 공통 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `code` | `String` | 서버 정의 에러 코드 |
| `message` | `String` | 에러 메시지 |
| `details` | `String[]` | 유효성 검증 실패 등 상세 정보 |
| `timestamp` | `String(ISO-8601)` | 에러 발생 시각 |

---

# 4. 에러 코드

| HTTP Status | Code | Message | 설명 |
|---|---|---|---|
| `400 Bad Request` | `INVALID_REQUEST` | `요청 값이 올바르지 않습니다.` | 요청 DTO 검증 실패 |
| `401 Unauthorized` | `AUTH_INVALID_CREDENTIALS` | `이메일 또는 비밀번호가 올바르지 않습니다.` | 로그인 실패 |
| `401 Unauthorized` | `AUTH_INVALID_ACCESS_TOKEN` | `Access Token이 올바르지 않습니다.` | Access Token 누락/형식 오류/검증 실패 |
| `401 Unauthorized` | `AUTH_INVALID_REFRESH_TOKEN` | `Refresh Token이 올바르지 않습니다.` | Refresh Token 미존재/만료/재사용 |
| `403 Forbidden` | `AUTH_INACTIVE_USER` | `비활성화된 사용자입니다.` | 비활성 사용자 |
| `403 Forbidden` | `ACCESS_DENIED` | `접근 권한이 없습니다.` | 권한 부족 |
| `409 Conflict` | `AUTH_DUPLICATE_EMAIL` | `이미 사용 중인 이메일입니다.` | 중복 이메일 회원가입 |
| `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | `요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.` | 호출 제한 초과 |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | `서버 오류가 발생했습니다.` | 서버 내부 오류 |

---

# 5. Rate Limit

서버는 메모리 기반 Rate Limiter를 사용합니다.

| API | 기준 | 제한 |
|---|---|---|
| 회원가입 | IP | 60초당 10회 |
| 회원가입 | 이메일 | 3600초당 3회 |
| 로그인 | IP | 60초당 20회 |
| 로그인 | 이메일 | 900초당 5회 |
| 토큰 재발급 | IP | 60초당 60회 |
| 토큰 재발급 | Refresh Token | 60초당 10회 |
| 로그아웃 | IP | 60초당 60회 |
| 로그아웃 | Refresh Token | 60초당 10회 |

제한 초과 시:

```http
429 Too Many Requests
```

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

---

# 6. API 상세

## 6.1 헬스 체크

### `GET /health`

서버 실행 여부를 확인합니다.

### Request

요청 본문 없음

### Response

#### `200 OK`

```text
OK
```

---

## 6.2 회원가입

### `POST /api/auth/signup`

신규 사용자를 생성합니다.

### Request Body

```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "jch"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---:|---|
| `email` | `String` | O | 공백 불가, 이메일 형식 |
| `password` | `String` | O | 공백 불가, 8~100자 |
| `nickname` | `String` | O | 공백 불가, 2~30자 |

### Success Response

#### `201 Created`

```json
{
  "userId": 1,
  "email": "test@example.com",
  "nickname": "jch",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2026-05-10T09:00:00Z"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `userId` | `Long` | 사용자 ID |
| `email` | `String` | 정규화된 이메일 |
| `nickname` | `String` | 앞뒤 공백이 제거된 닉네임 |
| `role` | `String` | 사용자 권한. 기본값 `USER` |
| `status` | `String` | 사용자 상태. 기본값 `ACTIVE` |
| `createdAt` | `String(ISO-8601)` | 생성 시각 |

### Error Responses

#### `400 Bad Request`

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "details": [
    "email: must be a well-formed email address",
    "password: size must be between 8 and 100",
    "nickname: must not be blank"
  ],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `409 Conflict`

```json
{
  "code": "AUTH_DUPLICATE_EMAIL",
  "message": "이미 사용 중인 이메일입니다.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `429 Too Many Requests`

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

---

## 6.3 로그인

### `POST /api/auth/login`

이메일과 비밀번호를 검증한 뒤 토큰 정보와 로그인 사용자 정보를 반환합니다.

### Request Body

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---:|---|
| `email` | `String` | O | 공백 불가, 이메일 형식 |
| `password` | `String` | O | 공백 불가, 8~100자 |

### Success Response

#### `200 OK`

```json
{
  "token": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "random-refresh-token-value",
    "accessTokenExpiresAt": "2026-05-10T09:15:00Z",
    "refreshTokenExpiresAt": "2026-05-24T09:00:00Z"
  },
  "user": {
    "userId": 1,
    "email": "test@example.com",
    "nickname": "jch",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

### Response Fields

#### `token`

| 필드 | 타입 | 설명 |
|---|---|---|
| `accessToken` | `String` | JWT Access Token |
| `refreshToken` | `String` | 랜덤 문자열 Refresh Token |
| `accessTokenExpiresAt` | `String(ISO-8601)` | Access Token 만료 시각 |
| `refreshTokenExpiresAt` | `String(ISO-8601)` | Refresh Token 만료 시각 |

#### `user`

| 필드 | 타입 | 설명 |
|---|---|---|
| `userId` | `Long` | 로그인 사용자 ID |
| `email` | `String` | 로그인 사용자 이메일 |
| `nickname` | `String` | 로그인 사용자 닉네임 |
| `role` | `String` | 사용자 권한 |
| `status` | `String` | 사용자 상태 |

### Error Responses

#### `400 Bad Request`

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "details": [
    "email: must be a well-formed email address"
  ],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `401 Unauthorized`

```json
{
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `403 Forbidden`

```json
{
  "code": "AUTH_INACTIVE_USER",
  "message": "비활성화된 사용자입니다.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `429 Too Many Requests`

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

---

## 6.4 토큰 재발급

### `POST /api/auth/refresh`

Refresh Token을 사용해 새 Access Token과 새 Refresh Token을 발급하고, 사용자 정보를 함께 반환합니다.

### Request Body

```json
{
  "refreshToken": "random-refresh-token-value"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---:|---|
| `refreshToken` | `String` | O | 공백 불가 |

### Success Response

#### `200 OK`

```json
{
  "token": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token",
    "accessTokenExpiresAt": "2026-05-10T09:30:00Z",
    "refreshTokenExpiresAt": "2026-05-24T09:15:00Z"
  },
  "user": {
    "userId": 1,
    "email": "test@example.com",
    "nickname": "jch",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

### 처리 규칙

1. 전달된 Refresh Token을 해시하여 DB에서 조회
2. 토큰이 존재하지 않으면 `401 AUTH_INVALID_REFRESH_TOKEN`
3. 토큰이 이미 폐기된 상태라면:
   - 해당 사용자의 활성 Refresh Token 전체 폐기
   - `401 AUTH_INVALID_REFRESH_TOKEN`
4. 토큰이 만료되었으면 `401 AUTH_INVALID_REFRESH_TOKEN`
5. 토큰 사용자 조회 실패 시 `401 AUTH_INVALID_REFRESH_TOKEN`
6. 사용자가 비활성 상태면 `403 AUTH_INACTIVE_USER`
7. 정상 토큰이면:
   - 기존 Refresh Token 폐기
   - 새 Access Token 발급
   - 새 Refresh Token 발급
   - 새 Refresh Token 저장
   - 새 토큰 정보와 사용자 정보 반환

### Error Responses

#### `400 Bad Request`

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "details": [
    "refreshToken: must not be blank"
  ],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `401 Unauthorized`

```json
{
  "code": "AUTH_INVALID_REFRESH_TOKEN",
  "message": "Refresh Token이 올바르지 않습니다.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `403 Forbidden`

```json
{
  "code": "AUTH_INACTIVE_USER",
  "message": "비활성화된 사용자입니다.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `429 Too Many Requests`

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

---

## 6.5 로그아웃

### `POST /api/auth/logout`

전달된 Refresh Token을 폐기합니다.

### Request Body

```json
{
  "refreshToken": "random-refresh-token-value"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---:|---|
| `refreshToken` | `String` | O | 공백 불가 |

### Success Response

#### `204 No Content`

응답 본문 없음

### 처리 규칙

- 전달된 Refresh Token이 존재하고 아직 폐기되지 않은 상태라면 폐기
- 존재하지 않는 Refresh Token이거나 이미 폐기된 Refresh Token이어도 예외 없이 `204 No Content` 반환
- 클라이언트 관점에서 로그아웃 API는 멱등적으로 처리 가능

### Error Responses

#### `400 Bad Request`

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청 값이 올바르지 않습니다.",
  "details": [
    "refreshToken: must not be blank"
  ],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

#### `429 Too Many Requests`

```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청 횟수가 너무 많습니다. 잠시 후 다시 시도해 주세요.",
  "details": [],
  "timestamp": "2026-05-10T09:00:00Z"
}
```

---

# 7. 인증 흐름

```text
1. 회원가입
   POST /api/auth/signup

2. 로그인
   POST /api/auth/login
   → token + user 수신

3. 보호 API 호출
   Authorization: Bearer {accessToken}

4. Access Token 만료
   POST /api/auth/refresh
   → 새 token + user 수신
   → 기존 refreshToken은 폐기

5. 로그아웃
   POST /api/auth/logout
   → refreshToken 폐기
```

---

# 8. iOS 연동 시 권장 처리

## 8.1 DTO 구조

최신 서버 응답 기준으로 iOS에서는 로그인 / 재발급 응답을 아래처럼 분리하는 것이 자연스럽습니다.

```swift
struct LoginResponseDTO: Decodable {
    let token: TokenResponseDTO
    let user: AuthUserResponseDTO
}

struct TokenResponseDTO: Decodable {
    let accessToken: String
    let refreshToken: String
    let accessTokenExpiresAt: Date
    let refreshTokenExpiresAt: Date
}

struct AuthUserResponseDTO: Decodable {
    let userId: Int
    let email: String
    let nickname: String
    let role: String
    let status: String
}
```

## 8.2 토큰 저장

| 토큰 | 권장 저장 위치 |
|---|---|
| Access Token | 메모리 우선, 필요 시 Keychain |
| Refresh Token | Keychain |

## 8.3 재발급 시 유의점

`refresh` 성공 시 **Access Token뿐 아니라 Refresh Token도 반드시 새 값으로 교체**해야 합니다.

잘못된 처리:

```text
새 accessToken만 저장
기존 refreshToken 유지
```

올바른 처리:

```text
새 accessToken 저장
새 refreshToken 저장
기존 refreshToken 폐기
```

## 8.4 로그인 / 재발급 응답 활용

이제 로그인과 재발급 응답에 `user`가 함께 포함되므로:

- 로그인 직후 별도 사용자 조회 API 없이 사용자 기본 정보를 보관 가능
- 재발급 후에도 최신 사용자 정보를 함께 갱신 가능
- iOS의 인증 상태 모델을 `token + user` 단위로 구성하기 좋음

## 8.5 로그아웃 처리

로그아웃 성공 여부와 관계없이 클라이언트 로컬 토큰은 삭제하는 편이 안전합니다.

---

# 9. cURL 예시

## 회원가입

```bash
curl -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "nickname": "jch"
  }'
```

## 로그인

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 토큰 재발급

```bash
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token"
  }'
```

## 로그아웃

```bash
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token"
  }'
```

---

# 10. 구현 기준

이 문서는 최신 업로드 소스의 아래 파일을 기준으로 작성했습니다.

- `AuthController`
- `SignupRequest`
- `SignupResponse`
- `LoginRequest`
- `LoginResponse`
- `TokenResponse`
- `AuthUserResponse`
- `RefreshTokenRequest`
- `SignupUseCase`
- `LoginUseCase`
- `RefreshTokenUseCase`
- `LogoutUseCase`
- `ErrorCode`
- `GlobalExceptionHandler`
- `SecurityConfig`
- `EndpointRateLimiter`
- `JwtAuthenticationFilter`
- `JwtAuthenticationEntryPoint`
- `JwtAccessDeniedHandler`
- `HealthController`
- `application.yaml`

