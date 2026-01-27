package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import store.onuljang.config.KakaoPayConfigDto;
import store.onuljang.exception.CustomRuntimeException;
import store.onuljang.exception.KakaoPayException;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class KakaoPayService {
    KakaoPayConfigDto kakaoPayConfigDto;
    RestTemplate restTemplate = new RestTemplate();

    public KakaoPayReadyResponse ready(KakaoPayReadyRequest request) {
        String url = kakaoPayConfigDto.getHost() + "/v1/payment/ready";
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(buildReadyBody(request), buildHeaders());
        ResponseEntity<KakaoPayReadyResponse> response = restTemplate.postForEntity(url, entity,
            KakaoPayReadyResponse.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new KakaoPayException("카카오페이 결제 준비에 실패했습니다.");
        }
        return response.getBody();
    }

    public KakaoPayApproveResponse approve(KakaoPayApproveRequest request) {
        String url = kakaoPayConfigDto.getHost() + "/v1/payment/approve";
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(buildApproveBody(request), buildHeaders());
        ResponseEntity<KakaoPayApproveResponse> response = restTemplate.postForEntity(url, entity,
            KakaoPayApproveResponse.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new KakaoPayException("카카오페이 결제 승인에 실패했습니다.");
        }
        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoPayConfigDto.getAdminKey());
        return headers;
    }

    private MultiValueMap<String, String> buildReadyBody(KakaoPayReadyRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", kakaoPayConfigDto.getCid());
        body.add("partner_order_id", request.partnerOrderId());
        body.add("partner_user_id", request.partnerUserId());
        body.add("item_name", request.itemName());
        body.add("quantity", String.valueOf(request.quantity()));
        body.add("total_amount", String.valueOf(request.totalAmount()));
        body.add("tax_free_amount", "0");
        body.add("approval_url", request.approvalUrl());
        body.add("cancel_url", request.cancelUrl());
        body.add("fail_url", request.failUrl());
        return body;
    }

    private MultiValueMap<String, String> buildApproveBody(KakaoPayApproveRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", kakaoPayConfigDto.getCid());
        body.add("tid", request.tid());
        body.add("partner_order_id", request.partnerOrderId());
        body.add("partner_user_id", request.partnerUserId());
        body.add("pg_token", request.pgToken());
        return body;
    }
}
