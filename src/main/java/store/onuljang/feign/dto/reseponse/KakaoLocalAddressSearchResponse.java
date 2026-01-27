package store.onuljang.feign.dto.reseponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoLocalAddressSearchResponse(List<KakaoLocalDocument> documents) {}
