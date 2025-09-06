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
2. 예약 마감 시간: 제품 판매 당일 18시까지 예약 가능합니다.
3. 매장은 24시간 운영되지만 직원은 19시까지만 상주합니다. (19시 이후 무인 매장으로 운영됩니다)
   - 19시까지 찾아가지 않은 예약은 자동으로 취소됩니다.
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

| 이슈 | 해결 방법 | 관련 파일 |
|------|-----------|-----------|
| 멱등성 보장 정산 구현 <br>(전날 취소 상품 정산 반영) | 1. 정산, 작업 테이블 활용. 스케줄러 동작 시 “작업 테이블 데이터 → 정산” 방식 적용<br>2. 정산 테이블은 “예약 PK + *매출 발생 유형” 복합 Unique key를 적용해 멱등성 보장<br>3. 작업 테이블은 “판매일 + 상품 PK” 복합 Unique key를 적용해 멱등성 보장 | [`ReservationAggregationScheduler.java`](./src/main/java/store/onuljang/scheduler/ReservationAggregationScheduler.java) <br>[`AdminAggregationAppService.java`](./src/main/java/store/onuljang/appservice/AdminAggregationAppService.java)|
| 노쇼 고객 재고 복원 정합성 해결 <br>(예약 마감 이후 노쇼 고객 상품 재고 복원) | 1. 복구 예정 상품 수량을 GROUP BY로 가져와서 재고 복원 <br>2. 복원 예상 row와 실제 반영 row를 비교해, 불일치시 예외를 발생시키는 방식으로 정합성을 보장 <br>3. 재고 복원시 Lock 점유 시간 최소화를 위해 반복문을 돌며 하나의 상품에만 Lock 점유 <br>4.  DB Deadlock / LockTimeout / 2번 작업 재고 불일치 등  지정 예외 발생 시 최대 3회 재시도 적용 | [`ReservationResetScheduler.java`](./src/main/java/store/onuljang/scheduler/ReservationResetScheduler.java)<br>[`ReservationAppService.java`](./src/main/java/store/onuljang/appservice/ReservationAppService.java) |
| 재고 정합성 문제 | 초기에는 예약 관련 코드별로 락 획득 순서가 일관되지 않아 데드락/락 타임아웃으로 요청 실패가 간헐적으로 발생<br>이후 Product → User 순으로 락 획득 순서를 전역적으로 고정하여 데드락 발생 가능성을 제거하고, 재고 차감/복원이 항상 원자적·일관적으로 처리되도록 개선 | [`ReservationAppService.java`](./src/main/java/store/onuljang/appservice/ReservationAppService.java) |
| 파일 업로드 메모리 초과 | AWS S3 Presigned URL 방식 적용, 다건 업로드 시 프론트엔드 병렬 처리로 개선 | [`AdminUploadService.java`](./src/main/java/store/onuljang/service/AdminUploadService.java) |
| 과도한 카카오 로그인 API 요청 | Refresh Token 저장 + 로그인 시 `/auth/refresh` 호출로 개선 | [`AuthAppService.java`](./src/main/java/store/onuljang/appservice/ProdAuthAppServiceImpl.java) |

---

## 🧩 운영/관리를 위한 주요 기능

| 기능 | 설명 | 관련 파일 |
|------|------|-----------|
| S3에 로그 파일 업로드 | warn 이상 / 모든 level 로그 파일을 매일 s3에 업로드 | [`logback-spring.xml`](./src/main/resources/logback-spring.xml)<br>[`LogUploadScheduler.java`](./src/main/java/store/onuljang/scheduler/LogUploadScheduler.java)|
| 유저/관리자 행위 로깅 | `UserLog`, `AdminLog` 테이블을 통해 주요 행동 기록 | [`UserLog.java`](./src/main/java/store/onuljang/repository/entity/log/UserLog.java)<br>[`UserReservationLog.java`](./src/main/java/store/onuljang/repository/entity/log/UserReservationLog.java)<br>[`AdminLog.java`](./src/main/java/store/onuljang/repository/entity/log/AdminProductLog.java)<br>[`AdminProductLog.java`](./src/main/java/store/onuljang/repository/entity/log/AdminLog.java) |
| Refresh Token 관리 | DB에 해시 형태로 저장되고 `replaced_by` 컬럼으로 linked-list 형식 추적 가능 | [`RefreshToken.java`](./src/main/java/store/onuljang/repository/entity/RefreshToken.java) |
| 관리자 권한 검증 강화 | Spring Security 필터, `hasRole`, validate API 추가로 미검증 방지 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java) |
| 관리자 인증 커스터마이징 | 세션에 관리자 ID 저장 위해 `AdminUserDetail`, `AdminAuthenticationToken` 구현 | [`AdminSecurityConfig.java`](./src/main/java/store/onuljang/auth/AdminSecurityConfig.java)<br>[`AdminUserDetail.java`](./src/main/java/store/onuljang/service/dto/AdminUserDetails.java)<br>[`AdminAuthenticationToken.java`](./src/main/java/store/onuljang/auth/AdminAuthenticationToken.java) |

---
## 🧪 향후 개선 예정 (TODO)
- [x] **예약 마감 이후 미수령분 일괄취소/재고복원**: 매출/예약 관리 편의성을 위함
- [x] **예약 마감 시간 자동 비활성화**: 일정 시간 이후 당일 판매 상품 예약 마감 처리
- [x] **관리자 판매량 조회 집계 기능 적용**: 판매량 조회 시 예약 테이블 full scan으로 인한 성능 저하 우려
- [x] **로그 데이터 스케줄러 활용 S3 업로드**: 일회성 로그 데이터 관리 필요성
- [ ] **cron 활용 DB 백업 S3 업로드**: DB 백업 필요성
- [x] **날짜별 품목 노출 순서 기능**: 제품 수 증가로 노출 품목의 순서 지정 필요성
- [ ] **상품 카테고리화 + 사용자 노출 페이지 적용**: 제품량 증가로 인해 사용자에게 보일 품목의 순서 지정 필요성
- [ ] **관리자 상품 조회 페이지 개선**: 수십건 조회시 특정 품목을 찾기 어려움
- [ ] **테스트 코드 작성**: 단위 테스트/통합 테스트 검증 필요
- [ ] ~~최고 관리자 권한 기능 도입: 다른 관리자의 권한 생성/수정/삭제 가능하도록 확장~~ 보류
- [ ] ~~결제 연동 도입: PG사 연동~~ 보류

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

- 사용자는 `카카오 OAuth → 자체 JWT 발급 → 쿠키 저장` 방식
- 관리자는 세션 쿠키를 통한 상태 기반 인증 유지
