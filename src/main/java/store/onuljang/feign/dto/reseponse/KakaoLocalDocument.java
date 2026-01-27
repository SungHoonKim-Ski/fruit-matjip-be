package store.onuljang.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoLocalDocument(String x, String y) {}
