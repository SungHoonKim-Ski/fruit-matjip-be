package store.onuljang.shared.notification.kakao.dto;

import java.util.Map;

public record KakaoNotificationMessage(
    String tplCode,
    String receiverPhone,
    String receiverName,
    Map<String, String> variables,
    String buttonUrl
) {}
