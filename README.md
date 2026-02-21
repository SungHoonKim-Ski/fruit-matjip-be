# 🍽️ 과일맛집 - 백엔드

공동구매 예약 **과일맛집**의 백엔드 레포지토리입니다.  
관리자/사용자용 서비스의 서버 기능을 담당하며, Spring Boot 기반 RESTful API 서버로 동작합니다.

---

# 🎯 프로젝트 배경 & 문제 정의
<img width="200" height="500" alt="image" src="https://github.com/user-attachments/assets/29b6769c-b0a8-4de5-b894-6b2abbfdcb81" />

- 문제: 카카오톡으로 재고를 관리하고 예약을 받다 보니 피로도가 높고 주문 누락이 종종 발생했습니다.
- 목표(해결하고 싶은 문제): 하나의 공통된 채널(웹) 에서 재고·예약을 일원화해 피로도를 낮추고 누락을 방지합니다.

## 📅 예약/취소 비즈니스 규칙
1. 소비자는 웹에서 상품 예약 후, 오프라인 매장에서 결제하고 수령합니다.
2. 예약 마감 시간: 제품 판매 당일 19시 30분까지 예약 가능합니다.
3. 매장은 24시간 운영되지만 직원은 19시까지만 상주합니다. (19시 이후 무인 매장으로 운영됩니다)
   - 19시까지 찾아가지 않은 예약은 자동으로 취소됩니다.
   - 자동 취소 배치는 매일 20시 03분(KST)에 실행됩니다.
   - 19시 이후 수령을 원하는 경우, 예약 상태를 셀프 수령으로 변경하면 무인 운영 시간에 수령 가능합니다.
4. 셀프 수령 여부는 다음날 확인하며 미수령시 경고를 부여합니다.
   - 경고 2회 누적 시 해당 월에는 셀프 수령 서비스 이용이 불가능합니다.
   - 매월 1일에 모든 고객의 셀프 수령 경고 횟수가 초기화됩니다.


## 🌐 배포 링크

| 환경 | 유저용 | 관리자용                              |
|------|--------|-----------------------------------|
| 운영 | https://fruit-matjip.store | https://fruit-matjip.store/admin      |
| 테스트 | https://dev.fruit-matjip.store | https://dev.fruit-matjip.store/admin |

+ 프론트엔드 레포: [onuljang-fe](https://github.com/SungHoonKim-Ski/onuljang-fe)

---

## 🖼️ 시스템 아키텍처
<img width="1778" height="638" alt="image" src="https://github.com/user-attachments/assets/df914dc7-d0c4-4757-8d97-e8c36d80ab85" />

---

## 🛠️ 주요 이슈 해결 사례

### 1. 멱등성 보장 정산 구현

**문제**  
전날 취소된 상품의 매출을 정산에 반영해야 하는데, 스케줄러가 중복 실행되거나 재시도될 경우 중복 집계 위험

**해결 방법**
- 정산 테이블(`product_daily_agg`)과 작업 테이블(`agg_applied`) 분리
- 스케줄러 동작: `작업 테이블 데이터 → 정산 테이블` 순차 처리
- **멱등성 보장**:
  - 정산 테이블: `(예약 PK + 매출 발생 유형)` 복합 Unique Key
  - 작업 테이블: `(판매일 + 상품 PK)` 복합 Unique Key

**관련 파일**  
[`ReservationAggregationScheduler.java`](./src/main/java/store/onuljang/scheduler/ReservationAggregationScheduler.java), [`AdminAggregationAppService.java`](./src/main/java/store/onuljang/appservice/AdminAggregationAppService.java)

---

### 2. 노쇼 고객 재고 복원 정합성

**문제**  
예약 마감 이후 노쇼 고객의 상품 재고를 복원할 때, 동시성 이슈로 인한 재고 불일치 발생 가능성

**해결 방법**
1. **GROUP BY 집계**: 복구 예정 상품 수량을 미리 계산
2. **정합성 검증**: 복원 예상 row 수와 실제 반영 row 수 비교, 불일치 시 예외 발생
3. **Lock 최적화**: 반복문으로 상품별 개별 Lock 획득하여 점유 시간 최소화
4. **재시도 로직**: Deadlock/LockTimeout/재고 불일치 시 최대 3회 자동 재시도

**관련 파일**  
[`ReservationResetScheduler.java`](./src/main/java/store/onuljang/scheduler/ReservationResetScheduler.java), [`ReservationAppService.java`](./src/main/java/store/onuljang/appservice/ReservationAppService.java)

---

### 3. 재고 정합성 문제

**문제**  
예약 관련 코드에서 Lock 획득 순서가 일관되지 않아 Deadlock 및 Lock Timeout으로 요청 실패 발생

**해결 방법**  
전역적으로 **Product → User 순서**로 Lock 획득 순서를 고정하여:
- Deadlock 발생 가능성 제거
- 재고 차감/복원이 항상 원자적·일관적으로 처리

**관련 파일**  
[`ReservationAppService.java`](./src/main/java/store/onuljang/appservice/ReservationAppService.java)

---

### 4. 파일 업로드 메모리 초과

**문제**  
대용량 이미지 업로드 시 서버 메모리 부담 및 타임아웃 발생

**해결 방법**  
AWS S3 Presigned URL 방식 도입:
- 클라이언트가 직접 S3에 업로드
- 다건 업로드 시 프론트엔드에서 병렬 처리
- 서버 부하 최소화

**관련 파일**  
[`AdminUploadService.java`](./src/main/java/store/onuljang/service/AdminUploadService.java)

---

### 5. 과도한 카카오 로그인 API 요청

**문제**  
매 로그인마다 카카오 API를 호출하여 불필요한 외부 API 의존성 및 응답 지연

**해결 방법**  
Refresh Token 기반 인증 개선:
- Refresh Token을 DB에 저장
- 로그인 시 `/auth/refresh` 엔드포인트로 토큰 갱신
- 카카오 API 호출 최소화

**관련 파일**  
[`ProdAuthAppServiceImpl.java`](./src/main/java/store/onuljang/appservice/ProdAuthAppServiceImpl.java)


---

## 🧩 운영/관리를 위한 주요 기능

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| S3에 로그 파일 업로드 | warn 이상 / 모든 level 로그 파일을 매일 s3에 업로드 | [`logback-spring.xml`](./src/main/resources/logback-spring.xml)<br>[`LogUploadScheduler.java`](./src/main/java/store/onuljang/scheduler/LogUploadScheduler.java)|
| 유저/관리자 행위 로깅 | `UserLog`, `AdminLog` 테이블을 통해 주요 행동 기록 | [`UserLog.java`](./src/main/java/store/onuljang/repository/entity/log/UserLog.java)<br>[`UserReservationLog.java`](./src/main/java/store/onuljang/repository/entity/log/UserReservationLog.java)<br>[`AdminLog.java`](./src/main/java/store/onuljang/repository/entity/log/AdminProductLog.java)<br>[`AdminProductLog.java`](./src/main/java/store/onuljang/repository/entity/log/AdminLog.java) |
| Refresh Token 관리 | DB에 해시 형태로 저장되고 `replaced_by` 컬럼으로 linked-list 형식 추적 가능 | [`RefreshToken.java`](./src/main/java/store/onuljang/repository/entity/RefreshToken.java) |
| 관리자 권한 검증 강화 | Spring Security 필터, `hasRole`, validate API 추가로 미검증 방지 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java) |
| 관리자 인증 커스터마이징 | 세션에 관리자 ID 저장 위해 `AdminUserDetail`, `AdminAuthenticationToken` 구현 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java)<br>[`AdminUserDetail.java`](./src/main/java/store/onuljang/service/dto/AdminUserDetails.java)<br>[`AdminAuthenticationToken.java`](./src/main/java/store/onuljang/auth/AdminAuthenticationToken.java) |
| Brute Force 차단 | 관리자 로그인 5회 실패 시 15분 잠금 (인메모리) | [`LoginAttemptService.java`](./src/main/java/store/onuljang/auth/LoginAttemptService.java) |
| CSRF 보호 | 관리자 API에 CSRF 토큰 검증 적용, 프로파일별 쿠키 도메인 분리 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java)<br>[`CsrfCookieFilter.java`](./src/main/java/store/onuljang/auth/CsrfCookieFilter.java) |
| 배달 결제 관리 | 카카오페이 결제 연동, 미결제 자동 만료, 결제 상태 대사 | [`DeliveryPaymentExpireScheduler.java`](./src/main/java/store/onuljang/scheduler/DeliveryPaymentExpireScheduler.java)<br>[`DeliveryPaymentReconciliationScheduler.java`](./src/main/java/store/onuljang/scheduler/DeliveryPaymentReconciliationScheduler.java) |

---
## 🧪 향후 개선 예정 (TODO)
- [x] **예약 마감 이후 미수령분 일괄취소/재고복원**: 매출/예약 관리 편의성을 위함
- [x] **예약 마감 시간 자동 비활성화**: 일정 시간 이후 당일 판매 상품 예약 마감 처리
- [x] **관리자 판매량 조회 집계 기능 적용**: 판매량 조회 시 예약 테이블 full scan으로 인한 성능 저하 우려
- [x] **로그 데이터 스케줄러 활용 S3 업로드**: 일회성 로그 데이터 관리 필요성
- [x] **cron 활용 DB 백업 S3 업로드**: DB 백업 필요성 - 매일 1시 업로드
- [x] **날짜별 품목 노출 순서 기능**: 제품 수 증가로 노출 품목의 순서 지정 필요성
- [x] **관리자 상품 조회 페이지 개선**: 검색 기능 도입
- [x] **테스트 코드 작성**: 단위 테스트 / 통합 테스트 적용 완료
- [x] **배달 연계 / 결제 도입**: 카카오페이 연동 완료
- [ ] ~~최고 관리자 권한 기능 도입: 다른 관리자의 권한 생성/수정/삭제 가능하도록 확장~~ 보류
- [x] **상품 카테고리화**: 제품량 증가로 인해 사용자에게 보일 품목의 순서 지정 필요성


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

### 주요 테스트 파일

42개 테스트 파일, 330개 테스트 메서드

| 테스트 파일 | 설명 | 테스트 수 |
|------------|------|----------|
| [`AdminProductIntegrationTest`](./src/test/java/store/onuljang/integration/AdminProductIntegrationTest.java) | 관리자 상품 관리 API (CRUD/카테고리/이미지/순서) | 22개 |
| [`ReservationIntegrationTest`](./src/test/java/store/onuljang/integration/ReservationIntegrationTest.java) | 사용자 예약 API (생성/취소/수량변경/셀프픽업) | 14개 |
| [`ProductsIntegrationTest`](./src/test/java/store/onuljang/integration/ProductsIntegrationTest.java) | 사용자 상품 조회 API | 11개 |
| [`AdminCustomerFilterIntegrationTest`](./src/test/java/store/onuljang/integration/AdminCustomerFilterIntegrationTest.java) | 고객 관리 필터/정렬 API | 9개 |
| [`UserIntegrationTest`](./src/test/java/store/onuljang/integration/UserIntegrationTest.java) | 사용자 정보 관리 API | 9개 |
| [`UserRestrictionIntegrationTest`](./src/test/java/store/onuljang/integration/UserRestrictionIntegrationTest.java) | 이용제한/경고 API | 9개 |
| [`AdminReservationIntegrationTest`](./src/test/java/store/onuljang/integration/AdminReservationIntegrationTest.java) | 관리자 예약 관리 API | 7개 |
| [`ProductCategoryIntegrationTest`](./src/test/java/store/onuljang/integration/ProductCategoryIntegrationTest.java) | 상품 카테고리 API | 7개 |
| [`DeliveryReservationStatusIntegrationTest`](./src/test/java/store/onuljang/integration/DeliveryReservationStatusIntegrationTest.java) | 배달 예약 상태 관리 | 5개 |
| [`ConcurrentReservationIntegrationTest`](./src/test/java/store/onuljang/integration/ConcurrentReservationIntegrationTest.java) | 동시성 예약 시나리오 | 4개 |
| [`AdminAggregationIntegrationTest`](./src/test/java/store/onuljang/integration/AdminAggregationIntegrationTest.java) | 관리자 판매 집계 조회 API | 4개 |
| [`AdminLoginBruteForceIntegrationTest`](./src/test/java/store/onuljang/integration/AdminLoginBruteForceIntegrationTest.java) | 로그인 Brute Force 차단 | 4개 |
| [`AdminControllerIntegrationTest`](./src/test/java/store/onuljang/integration/AdminControllerIntegrationTest.java) | 관리자 인증/권한 API | 4개 |
| [`ReservationResetSchedulerTest`](./src/test/java/store/onuljang/scheduler/ReservationResetSchedulerTest.java) | 예약 초기화 스케줄러 | 3개 |
| [`DeliveryPaymentReconciliationSchedulerTest`](./src/test/java/store/onuljang/scheduler/DeliveryPaymentReconciliationSchedulerTest.java) | 카카오페이 결제 대사 스케줄러 | 5개 |
| [`DeliveryPaymentExpireSchedulerTest`](./src/test/java/store/onuljang/scheduler/DeliveryPaymentExpireSchedulerTest.java) | 미결제 주문 만료 스케줄러 | 4개 |
| [`ReservationAggregationSchedulerTest`](./src/test/java/store/onuljang/scheduler/ReservationAggregationSchedulerTest.java) | 예약 집계 스케줄러 | 4개 |

### CI/CD 통합

- **자동 테스트 실행**: PR 및 main/develop 브랜치 push 시 자동 실행
- **배포 전 검증**: 태그 기반 배포 시 테스트 통과 필수
- **테스트 리포트**: GitHub Actions에서 테스트 결과 및 리포트 자동 생성

```bash
# 로컬에서 전체 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests AdminReservationIntegrationTest
```

---

## ⚙️ 기술 스택

- Java 17 / Spring Boot 3.4
- Spring Security + JWT
- Spring Data JPA + QueryDSL (MySQL)
- Kakao OAuth 로그인
- KakaoPay 결제 연동
- AWS S3 + Presigned-url 기반 이미지 업로드
- Spring Session JDBC 기반 세션 관리
- OpenFeign (카카오 API 연동)

---

## 🔐 인증/인가 아키텍처

| 구분 | 인증 방식 | 상세 설명 |
|------|-----------|-----------|
| 사용자 | JWT + Kakao OAuth | Kakao 로그인 후 JWT (Access / Refresh) 발급 및 쿠키 저장 |
| 관리자 | Spring Session 기반 | JDBC 세션 저장소 사용, `ADMINSESSION` 쿠키 기반 로그인 유지 |

- 사용자는 `카카오 OAuth → 자체 JWT 발급 → 쿠키 저장` 방식
- 관리자는 세션 쿠키를 통한 상태 기반 인증 유지
- Refresh Token 재사용 감지 시 해당 사용자의 전체 토큰 일괄 revoke
- 관리자 로그인 Brute Force 차단 (5회 실패 → 15분 잠금)
- 관리자 API CSRF 보호 (CookieCsrfTokenRepository, 프로파일별 쿠키 도메인 분리)
- 보안 헤더 적용 (X-Frame-Options, HSTS, CSP, X-Content-Type-Options)
