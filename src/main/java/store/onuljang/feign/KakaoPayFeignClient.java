package store.onuljang.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import store.onuljang.feign.dto.reseponse.KakaoPayApproveResponse;
import store.onuljang.feign.dto.reseponse.KakaoPayReadyResponse;

@FeignClient(name = "KAKAO-PAY", url = "${KAKAOPAY.HOST}")
public interface KakaoPayFeignClient {
    @PostMapping(path = "/v1/payment/ready", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    KakaoPayReadyResponse ready(
        @RequestHeader("Authorization") String authorization,
        @RequestParam("cid") String cid,
        @RequestParam("partner_order_id") String partnerOrderId,
        @RequestParam("partner_user_id") String partnerUserId,
        @RequestParam("item_name") String itemName,
        @RequestParam("quantity") int quantity,
        @RequestParam("total_amount") int totalAmount,
        @RequestParam("tax_free_amount") int taxFreeAmount,
        @RequestParam("approval_url") String approvalUrl,
        @RequestParam("cancel_url") String cancelUrl,
        @RequestParam("fail_url") String failUrl
    );

    @PostMapping(path = "/v1/payment/approve", consumes = "application/x-www-form-urlencoded;charset=utf-8")
    KakaoPayApproveResponse approve(
        @RequestHeader("Authorization") String authorization,
        @RequestParam("cid") String cid,
        @RequestParam("tid") String tid,
        @RequestParam("partner_order_id") String partnerOrderId,
        @RequestParam("partner_user_id") String partnerUserId,
        @RequestParam("pg_token") String pgToken
    );
}
