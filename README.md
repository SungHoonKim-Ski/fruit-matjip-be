# 🍽️ 과일맛집 - 백엔드

공동구매 예약 **과일맛집**의 백엔드 레포지토리입니다.  
관리자/사용자용 서비스의 서버 기능을 담당하며, Spring Boot 기반 RESTful API 서버로 동작합니다.

---

## 🌐 배포 링크

| 환경 | 유저용 | 관리자용                              |
|------|--------|-----------------------------------|
| 운영 | https://onuljang.store | https://onuljang.store/admin      |
| 테스트 | https://dev.onuljang.store | https://dev.onuljang.store/admin |

+ 프론트엔드 레포: [onuljang-fe](https://github.com/SungHoonKim-Ski/onuljang-fe)

---

## 🖼️ 시스템 아키텍처
<img width="1778" height="638" alt="image" src="https://github.com/user-attachments/assets/df914dc7-d0c4-4757-8d97-e8c36d80ab85" />

---

## 📐 코드 계층 구조

해당 백엔드 프로젝트는 **도메인 계층 구분을 적용한 구조**로 설계했습니다.

```text
Controller
   ↓
AppService (use-case 단위)
   ↓
Service (세부 도메인 로직)
   ↓
Repository (JPA 기반 DB 접근)
```

| 계층        | 설명 |
|-------------|------|
| `Controller` | HTTP 요청 처리 및 응답 반환. 인증된 유저 정보를 받아 appService로 전달 |
| `AppService` | use-case 단위로 묶인 주요 기능 단위. 트랜잭션 단위로 처리됨 |
| `Service`    | 실제 비즈니스 도메인 중심의 내부 로직 담당 |
| `Repository` | JPA 기반 DB 접근 계층 (쿼리 메서드, fetch join 등 포함) |
---
## 📂 DB 테이블 구조

아래는 주요 테이블 및 관계의 간략한 설명입니다.
전체 구조는 ERD 이미지를 참고해주세요.

| 테이블 | 설명 |
|--------|------|
| `users` | 사용자 정보 저장 (카카오 ID, 닉네임) |
| `reservations` | 사용자 예약 내역 및 수량 정보 |
| `products` | 상품 정보 및 상태 (재고, 상품 노출 여부) |
| `admins` | 관리자 계정 및 권한 정보 (OWNER/MANAGER/NONE) |
| `admin_logs`, `user_logs` | API 호출 로깅 (메서드/응답 시간/상태 등) |

ERD
<img width="3448" height="2428" alt="prod-onuljang-server" src="https://github.com/user-attachments/assets/541891e3-00f5-48fd-b507-a5f216adad54" />

---

## 🚀 배포 전략

- GitHub Actions를 활용해 테스트/상용 환경 CI/CD 자동화
- **태그 푸시 시점에만 배포 수행**

```yaml
on:
  push:
    tags:
      - "[0-9]+\.[0-9]+\.[0-9]+-[dp]\.[0-9]+"
```

- 배포 분기 로직은 GitHub Actions 스크립트 내부에서 `-d`(dev), `-p`(prod) 접미어로 판단

---

## ⚙️ 기술 스택

- Java 17 / Spring Boot 3.4
- Spring Security + JWT
- Spring Data JPA (MySQL)
- Kakao OAuth 로그인
- AWS S3 + Presigned-url 기반 이미지 업로드
- Spring Session JDBC 기반 세션 관리
- OpenFeign (카카오 API 연동)

---

## 🔐 인증/인가 아키텍처

| 구분 | 인증 방식 | 상세 설명 |
|------|-----------|-----------|
| 사용자 | JWT + Kakao OAuth | Kakao 로그인 후 JWT (Access / Refresh) 발급 및 쿠키 저장 |
| 관리자 | Spring Session 기반 | JDBC 세션 저장소 사용, `ADMINSESSION` 쿠키 기반 로그인 유지 |

- 사용자는 `카카오 OAuth → Access Token → 쿠키 저장` 방식
- 관리자는 세션 쿠키를 통한 상태 기반 인증 유지

---

## 🛠️ 주요 이슈 해결 사례

| 이슈                                  | 해결 방법 | 관련 파일 |
|-------------------------------------|-----------|-----------|
| 재고 관련 동시성 문제                        | JPA의 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용 | [`ProductsRepository.java`](./src/main/java/store/onuljang/repository/ProductsRepository.java) |
| 카카오 로그인 시 자동 회원가입 + 고유 닉네임 생성 |1. 카카오 인증 -> 고유 ID 반환<br>2. DB에서 유저 확인<br>3. 미존재 시 닉네임 생성기 호출 (`@Lock` 사용)<br>4. 고유 닉네임 생성 및 회원가입<br>5. 로그인 처리 |[`AuthAppService.java`](./src/main/java/store/onuljang/appservice/ProdAuthAppServiceImpl.java)<br>[`NameGenerator.java`](./src/main/java/store/onuljang/service/NameGenerator.java) |
| 파일 업로드 메모리 초과                       | AWS S3 Presigned URL 방식 적용 | [`AdminUploadService.java`](./src/main/java/store/onuljang/service/AdminUploadService.java) |
| 과도한 카카오 로그인 API 요청                  | Refresh Token 저장 + 로그인 시 `/auth/refresh` 호출로 개선 | [`AuthAppService.java`](./src/main/java/store/onuljang/appservice/ProdAuthAppServiceImpl.java) |
| N+1 문제                              | JPA Fetch Join(`@EntityGraph`) 적용 | [`ReservationAllRepository.java`](./src/main/java/store/onuljang/repository/ReservationAllRepository.java) |

---

## 🧩 운영/관리를 위한 주요 기능

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| Refresh Token 관리 | DB에 해시 형태로 저장되고 `replaced_by` 컬럼으로 linked-list 형식 추적 가능 | [`RefreshToken.java`](./src/main/java/store/onuljang/repository/entity/RefreshToken.java) |
| 관리자 권한 검증 강화 | Spring Security 필터, `hasRole`, validate API 추가로 미검증 방지 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java) |
| 관리자 인증 커스터마이징 | 세션에 관리자 ID 저장 위해 `AdminUserDetail`, `AdminAuthenticationToken` 구현 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/config/AdminSecurityConfig.java)<br>[`AdminUserDetail.java`](./src/main/java/store/onuljang/service/dto/AdminUserDetails.java)<br>[`AdminAuthenticationToken.java`](./src/main/java/store/onuljang/auth/AdminAuthenticationToken.java) |
| 유저/관리자 행위 로깅 | `UserLog`, `AdminLog` 테이블을 통해 주요 행동 기록 | [`UserLog.java`](./src/main/java/store/onuljang/repository/entity/log/UserLog.java)<br>[`AdminLog.java`](./src/main/java/store/onuljang/repository/entity/log/AdminLog.java) |
| 환경 설정 분기 | CI 스크립트에서 dev/prod 프로필 분기 | [`deploy.yml`](.github/workflows/aws.yml) |

---

## 🧪 향후 개선 예정 (TODO)

- [ ] **테스트 코드 작성**: Service/Controller 단위에 대한 JUnit 기반 검증 필요
- [ ] **예약 마감 시간 자동 비활성화**: 일정 시간이 지나면 비활성 처리 자동화
- [ ] **상품 조회/관리자 집계 페이징 처리**: 수백 건 이상 조회 시 성능 저하 방지용
- [ ] **최고 관리자 권한 기능 도입**: 다른 관리자의 권한 생성/수정/삭제 가능하도록 확장
- [ ] **집계용 숫자 데이터 증가 대비 배치 처리 고려**: 시간 단위 통계 집계 시 처리량에 대비
- [ ] **결제 연동 도입**: PG사 연동 고려 필요 (토스/카카오페이 등과 계약 여부 논의)
