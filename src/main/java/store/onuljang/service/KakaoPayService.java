package store.onuljang.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.exception.KakaoPayException;
import store.onuljang.feign.KakaoPayFeignClient;
import store.onuljang.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayCancelResponse;
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
            KakaoPayReadyRequest fullRequest = new KakaoPayReadyRequest(
                kakaoPayConfigDto.getCid(),
                request.partnerOrderId(),
                request.partnerUserId(),
                request.itemName(),
                request.quantity(),
                request.totalAmount(),
                request.taxFreeAmount(),
                request.approvalUrl(),
                request.cancelUrl(),
                request.failUrl()
            );
            KakaoPayReadyResponse response = kakaoPayFeignClient.ready(
                buildAuthorizationHeader(), fullRequest);
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
            KakaoPayApproveRequest fullRequest = new KakaoPayApproveRequest(
                kakaoPayConfigDto.getCid(),
                request.tid(),
                request.partnerOrderId(),
                request.partnerUserId(),
                request.pgToken()
            );
            KakaoPayApproveResponse response = kakaoPayFeignClient.approve(
                buildAuthorizationHeader(), fullRequest);
            if (response == null) {
                throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다.");
            }
            return response;
        } catch (FeignException e) {
            throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다.");
        }
    }

    public KakaoPayCancelResponse cancel(KakaoPayCancelRequest request) {
        try {
            KakaoPayCancelRequest fullRequest = new KakaoPayCancelRequest(
                kakaoPayConfigDto.getCid(),
                request.tid(),
                request.cancelAmount(),
                request.cancelTaxFreeAmount()
            );
            KakaoPayCancelResponse response = kakaoPayFeignClient.cancel(
                buildAuthorizationHeader(), fullRequest);
            if (response == null) {
                throw new KakaoPayException("카카오페이 결제 취소에 실패했습니다.");
            }
            return response;
        } catch (FeignException e) {
            throw new KakaoPayException("카카오페이 결제 취소에 실패했습니다.");
        }
    }

    private String buildAuthorizationHeader() {
        return "SECRET_KEY " + kakaoPayConfigDto.getSecretKey();
    }
}
