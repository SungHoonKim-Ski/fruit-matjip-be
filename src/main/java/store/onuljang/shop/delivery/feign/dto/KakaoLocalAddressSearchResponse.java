package store.onuljang.shop.delivery.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoLocalAddressSearchResponse(List<KakaoLocalDocument> documents) {}
