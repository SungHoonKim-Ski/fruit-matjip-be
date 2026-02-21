package store.onuljang.shop.delivery.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoLocalDocument(String x, String y) {}
