package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import store.onuljang.config.KakaoConfigDto;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoLocalService {
    KakaoConfigDto kakaoConfigDto;
    RestTemplate restTemplate = new RestTemplate();

    public Optional<Coordinate> geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        String url = UriComponentsBuilder
            .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .queryParam("query", address)
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoConfigDto.getKakaoKey());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoAddressSearchResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, KakaoAddressSearchResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Optional.empty();
        }

        List<KakaoAddressSearchResponse.Document> documents = response.getBody().documents();
        if (documents == null || documents.isEmpty()) {
            return Optional.empty();
        }
        KakaoAddressSearchResponse.Document doc = documents.get(0);
        try {
            double lng = Double.parseDouble(doc.x());
            double lat = Double.parseDouble(doc.y());
            return Optional.of(new Coordinate(lat, lng));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public record Coordinate(double latitude, double longitude) {}

    public record KakaoAddressSearchResponse(List<Document> documents) {
        public record Document(String x, String y) {}
    }
}
