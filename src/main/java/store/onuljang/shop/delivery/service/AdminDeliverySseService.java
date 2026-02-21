package store.onuljang.shop.delivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import store.onuljang.shop.admin.dto.AdminDeliverySseResponse;
import store.onuljang.shop.delivery.entity.DeliveryOrder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class AdminDeliverySseService {
    private static final long TIMEOUT_MS = 60L * 60L * 1000L;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Scheduled(fixedRate = 30_000)
    public void heartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    public void notifyPaid(DeliveryOrder order) {
        AdminDeliverySseResponse payload = AdminDeliverySseResponse.from(order);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("delivery_paid").data(payload));
            } catch (Exception e) {
                log.warn("SSE 전송 실패, emitter 제거: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }
}
