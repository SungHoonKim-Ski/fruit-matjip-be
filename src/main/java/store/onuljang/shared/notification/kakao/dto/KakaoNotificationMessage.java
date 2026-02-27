package store.onuljang.shared.notification.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record KakaoNotificationMessage(
    @JsonProperty("tplCode") String tplCode,
    @JsonProperty("receiverPhone") String receiverPhone,
    @JsonProperty("receiverName") String receiverName,
    @JsonProperty("variables") Map<String, String> variables,
    @JsonProperty("buttonUrl") String buttonUrl
) {}
