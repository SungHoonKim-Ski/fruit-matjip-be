package store.onuljang.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import store.onuljang.controller.response.AdminDeliverySseResponse;
import store.onuljang.repository.entity.DeliveryOrder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminDeliverySseService {
    private static final long TIMEOUT_MS = 60L * 60L * 1000L;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    public void notifyPaid(DeliveryOrder order) {
        AdminDeliverySseResponse payload = AdminDeliverySseResponse.from(order);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("delivery_paid").data(payload));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
