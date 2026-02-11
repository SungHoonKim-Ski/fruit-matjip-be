ALTER TABLE message_template DROP INDEX message_type;

INSERT INTO message_template(title, body, message_type, priority)
VALUES ('🎉 예약배달 서비스 오픈 안내',
        '맛집퀵에 새로운 기능이 추가되었습니다!

<b>예약배달🕐</b> 서비스가 오픈되었습니다.

원하시는 시간에 맞춰
상품을 <b>집 앞까지 배달</b>해드립니다.

맛집퀵🚚 메뉴에서
<b>배달 희망 시간</b>을 선택해주세요.',
        'NOTICE',
        100);

INSERT INTO user_message_queue (user_uid, message_template_id, status, created_at)
SELECT u.uid, t.id, 'PENDING', NOW()
FROM users u
         CROSS JOIN (
    SELECT id FROM message_template
    WHERE message_type = 'NOTICE'
    ORDER BY id DESC
    LIMIT 1
) t;
