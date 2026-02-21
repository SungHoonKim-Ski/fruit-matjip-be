package store.onuljang.shop.delivery.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.shared.config.KakaoConfigDto;
import store.onuljang.shared.exception.GeocodeFailedException;
import store.onuljang.shop.delivery.feign.KakaoLocalFeignClient;
import store.onuljang.shop.delivery.feign.dto.KakaoLocalAddressSearchResponse;
import store.onuljang.shop.delivery.feign.dto.KakaoLocalDocument;

import feign.FeignException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoLocalService {
    KakaoConfigDto kakaoConfigDto;
    KakaoLocalFeignClient kakaoLocalFeignClient;

    public Coordinate geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new GeocodeFailedException("주소 좌표를 찾을 수 없습니다.");
        }
        try {
            KakaoLocalAddressSearchResponse response = kakaoLocalFeignClient.searchAddress(
                buildAuthorizationHeader(),
                address
            );
            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                throw new GeocodeFailedException("주소 좌표를 찾을 수 없습니다.");
            }
            KakaoLocalDocument doc = response.documents().get(0);
            double lng = Double.parseDouble(doc.x());
            double lat = Double.parseDouble(doc.y());
            return new Coordinate(lat, lng);
        } catch (FeignException | NumberFormatException e) {
            throw new GeocodeFailedException("주소 좌표를 찾을 수 없습니다.");
        }
    }

    public record Coordinate(double latitude, double longitude) {}

    private String buildAuthorizationHeader() {
        return "KakaoAK " + kakaoConfigDto.getKakaoKey();
    }
}
