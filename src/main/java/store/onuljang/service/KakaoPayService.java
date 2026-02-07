package store.onuljang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.exception.KakaoPayException;
import store.onuljang.feign.KakaoPayFeignClient;
import store.onuljang.feign.dto.request.KakaoPayApproveRequest;
import store.onuljang.feign.dto.request.KakaoPayCancelRequest;
import store.onuljang.feign.dto.request.KakaoPayOrderRequest;
import store.onuljang.feign.dto.request.KakaoPayReadyRequest;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayCancelResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayOrderResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayReadyResponse;

@Slf4j
@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoPayService {
    static final ObjectMapper MAPPER = new ObjectMapper();

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

    @Retryable(
        retryFor = FeignException.class,
        noRetryFor = FeignException.FeignClientException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public KakaoPayApproveResponse approve(KakaoPayApproveRequest request) {
        KakaoPayApproveRequest fullRequest = new KakaoPayApproveRequest(
            kakaoPayConfigDto.getCid(),
            request.tid(),
            request.partnerOrderId(),
            request.partnerUserId(),
            request.pgToken()
        );
        return kakaoPayFeignClient.approve(buildAuthorizationHeader(), fullRequest);
    }

    // 4xx: 재시도 없이 카카오페이 에러 메시지 파싱
    @Recover
    KakaoPayApproveResponse recoverClientError(FeignException.FeignClientException e, KakaoPayApproveRequest request) {
        throw new KakaoPayException(parseKakaoPayError(e, "카카오페이 결제 승인에 실패했습니다."));
    }

    // 5xx/네트워크: 재시도 소진
    @Recover
    KakaoPayApproveResponse recoverServerError(FeignException e, KakaoPayApproveRequest request) {
        log.error("카카오페이 결제 승인 재시도 소진: {}", e.getMessage());
        throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    public KakaoPayOrderResponse order(String tid) {
        try {
            KakaoPayOrderRequest request = new KakaoPayOrderRequest(
                kakaoPayConfigDto.getCid(), tid);
            return kakaoPayFeignClient.order(buildAuthorizationHeader(), request);
        } catch (FeignException e) {
            throw new KakaoPayException("카카오페이 주문 조회에 실패했습니다.");
        }
    }

    /**
     * 외부 PG 호출을 트랜잭션 밖에서 실행한다.
     * 호출측 트랜잭션에 참여하면 FeignException 발생 시 rollback-only로 마킹되어,
     * catch 후에도 커밋할 수 없는 문제가 발생한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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

    private String parseKakaoPayError(FeignException e, String fallback) {
        try {
            JsonNode node = MAPPER.readTree(e.contentUTF8());
            JsonNode extras = node.get("extras");
            if (extras != null && extras.has("method_result_message")) {
                return extras.get("method_result_message").asText();
            }
            if (node.has("error_message")) {
                return node.get("error_message").asText();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String buildAuthorizationHeader() {
        return "SECRET_KEY " + kakaoPayConfigDto.getSecretKey();
    }
}
