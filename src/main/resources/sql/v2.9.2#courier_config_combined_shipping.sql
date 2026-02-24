ALTER TABLE courier_config
    ADD COLUMN combined_shipping_enabled TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN combined_shipping_max_quantity INT NOT NULL DEFAULT 1;
안녕하세요, 택배 조회 API 사용 중 여쭤볼 내용이 있어 오픈채팅에 들어왔습니다.

무료 플랜으로 초기 도입 후 확장 시 스타터 플랜 사용 예정이며, 연동 전에 궁금한게 있어 문의드립니다.
참고로 월 배송 약 3,000건, 일 150~200건 규모 시스템 입니다.

1. 호출 카운트 범위: 월 호출 한도에 웹훅 구독 생성, 웹훅 콜백 수신, 배송 조회 API가 각각 포함되나요?
2. 웹훅 구독 제한: 하루 150~200건을 한 번에 등록할 예정인데요, 이 경우 제한에 걸리나요?
3. 무료 플랜의 "기본 웹훅"과 스타터의 "고급 웹훅"이 어떤 차이인가요?
4. 월 한도를 초과하면 즉시 차단인가요, 초과분 과금인가요?