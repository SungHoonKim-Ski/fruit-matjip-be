ALTER TABLE message_template ADD COLUMN priority INT NOT NULL DEFAULT 0;

UPDATE message_template SET priority = 50 WHERE message_type = 'USER_NO_SHOW';

INSERT INTO message_template(title, body, message_type, priority)
VALUES ('맛집퀵 서비스 오픈 안내',
        '오래 기다리셨습니다.
<b>맛집퀵🚚(QUICK) 서비스</b>가
드디어 오픈되었습니다!

예약하신 이후,
우측 하단 <b>맛집퀵🚚</b> 메뉴에서 결제하시면
상품을 <b>집으로 바로 배송</b>해드립니다.
<small>
배송비 안내
· 기본 배송비 2,900원
· 100m당 50원 추가
</small>',
        'NOTICE',
        100);

INSERT INTO user_message_queue (user_uid, message_template_id, status, created_at)
SELECT u.uid, t.id, 'PENDING', NOW()
FROM users u
         CROSS JOIN message_template t
WHERE t.message_type = 'NOTICE';