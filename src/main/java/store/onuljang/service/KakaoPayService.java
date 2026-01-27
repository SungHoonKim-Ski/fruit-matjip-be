package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.exception.KakaoPayException;
import store.onuljang.feign.KakaoPayFeignClient;
import store.onuljang.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayReadyResponse;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoPayService {
    KakaoPayConfigDto kakaoPayConfigDto;
    KakaoPayFeignClient kakaoPayFeignClient;

    public KakaoPayReadyResponse ready(KakaoPayReadyRequest request) {
        try {
            KakaoPayReadyResponse response = kakaoPayFeignClient.ready(
                buildAuthorizationHeader(),
                kakaoPayConfigDto.getCid(),
                request.partnerOrderId(),
                request.partnerUserId(),
                request.itemName(),
                request.quantity(),
                request.totalAmount(),
                0,
                request.approvalUrl(),
                request.cancelUrl(),
                request.failUrl()
            );
            if (response == null) {
                throw new KakaoPayException("카카오페이 결제 준비에 실패했습니다.");
            }
            return response;
        } catch (FeignException e) {
            throw new KakaoPayException("카카오페이 결제 준비에 실패했습니다.");
        }
    }

    public KakaoPayApproveResponse approve(KakaoPayApproveRequest request) {
        try {
            KakaoPayApproveResponse response = kakaoPayFeignClient.approve(
                buildAuthorizationHeader(),
                kakaoPayConfigDto.getCid(),
                request.tid(),
                request.partnerOrderId(),
                request.partnerUserId(),
                request.pgToken()
            );
            if (response == null) {
                throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다.");
            }
            return response;
        } catch (FeignException e) {
            throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다.");
        }
    }

    private String buildAuthorizationHeader() {
        return "KakaoAK " + kakaoPayConfigDto.getAdminKey();
    }
}
