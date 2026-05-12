package com.rs.subscription.controller;

import com.rs.subscription.event.PlanUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events (SSE) endpoint cho frontend-public.
 *
 * Tại sao chọn SSE thay vì polling/WebSocket:
 *  - SSE là HTTP đơn hướng (server→client), phù hợp cho "push thông báo cập nhật gói cước".
 *  - Tiêu tốn ít tài nguyên hơn WebSocket (không cần handshake 2 chiều).
 *  - Tự động reconnect phía browser, không cần thêm thư viện.
 *  - Có thể scale ngang (nhiều instance) bằng cách thay CopyOnWriteArrayList bằng
 *    Redis Pub/Sub (subscribe kênh "plan-updates" trên mỗi instance, rồi gọi broadcast()).
 *
 * Hiện tại dùng in-memory list (single-instance). Nếu cần multi-instance:
 *   1. Inject StringRedisTemplate
 *   2. Khi plan thay đổi: redisTemplate.convertAndSend("plan-updates", payload)
 *   3. Thêm @Bean MessageListenerAdapter → gọi broadcast()
 */
@RestController
@RequestMapping("/api/v1/public")
@Slf4j
public class PlanSseController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Frontend-public kết nối tại endpoint này để nhận thông báo real-time.
     * Kết nối được giữ mở vô thời hạn (Long.MAX_VALUE timeout).
     */
    @GetMapping(value = "/plan-updates/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(e -> emitters.remove(emitter));

        // Gửi comment keepalive ngay khi kết nối để tránh proxy timeout
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        log.debug("SSE client connected. Total: {}", emitters.size());
        return emitter;
    }

    /**
     * Lắng nghe PlanUpdatedEvent do IndividualPlanConfigServiceImpl phát ra,
     * rồi broadcast đến tất cả client đang kết nối.
     */
    @EventListener
    public void onPlanUpdated(PlanUpdatedEvent event) {
        String payload = "{\"planTemplateId\":" + event.getPlanTemplateId()
                + ",\"action\":\"" + event.getAction() + "\"}";
        broadcast(payload);
    }

    private void broadcast(String data) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("plan-updated").data(data));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
        log.debug("SSE broadcast to {} client(s), removed {} dead emitter(s)",
                emitters.size(), dead.size());
    }
}
