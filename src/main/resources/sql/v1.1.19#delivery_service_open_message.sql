ALTER TABLE message_template ADD COLUMN priority INT NOT NULL DEFAULT 0;

UPDATE message_template SET priority = 50 WHERE message_type = 'USER_NO_SHOW';

INSERT INTO message_template(title, body, message_type, priority)
VALUES ('맛집퀵 서비스 오픈 안내',
        '오래 기다리셨습니다.
<b>"맛집퀵(QUICK) 서비스"</b>가
드디어 오픈되었습니다!

하단 <b>상품배달</b> 항목을 누르셔서
결제해주시면
이제 <b>집으로 제품을 보내드립니다.</b>

*기본 배송비 2,900원
+ 100m당 50원 부과',
        'NOTICE',
        100);
