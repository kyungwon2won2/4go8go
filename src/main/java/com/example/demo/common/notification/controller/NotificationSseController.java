package com.example.demo.common.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/sse")
@Slf4j
public class NotificationSseController {
    
    // SSE 연결 관리 (사용자 이메일 -> SseEmitter)
    private static final Map<String, SseEmitter> EMITTERS = new ConcurrentHashMap<>();
    
    // 마지막 연결 시간 추적 (사용자 이메일 -> 타임스탬프)
    private static final Map<String, Long> LAST_CONNECT_TIMES = new ConcurrentHashMap<>();
    
    // SSE 연결 타임아웃 (30분)
    private static final long TIMEOUT = 30 * 60 * 1000L;
    
    // 최소 재연결 간격 (5초)
    private static final long MIN_RECONNECT_INTERVAL = 5000L;
    
    /**
     * SSE 연결 엔드포인트
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication, @RequestParam(required = false) Long t) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }
        
        String userEmail = authentication.getName();
        
        // 마지막 연결 시간 확인하여 빈번한 재연결 방지
        long now = System.currentTimeMillis();
        Long lastConnectTime = LAST_CONNECT_TIMES.get(userEmail);
        
        // 기존 연결 확인
        SseEmitter existingEmitter = EMITTERS.get(userEmail);
        
        // 마지막 연결 시도 후 MIN_RECONNECT_INTERVAL 이내에 다시 요청이 왔고,
        // 기존 emitter가 있으면 그대로 반환
        if (lastConnectTime != null && (now - lastConnectTime) < MIN_RECONNECT_INTERVAL && existingEmitter != null) {
            log.debug("SSE 재연결 간격이 너무 짧음, 기존 연결 유지: {} (간격: {}ms)", userEmail, now - lastConnectTime);
            return existingEmitter;
        }
        
        log.info("SSE 연결 요청: {}", userEmail);
        LAST_CONNECT_TIMES.put(userEmail, now);
        
        // 기존 연결이 있으면 제거
        if (existingEmitter != null) {
            removeEmitter(userEmail);
        }
        
        // 새 SSE Emitter 생성
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // 이벤트 처리 콜백 등록
        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료: {}", userEmail);
            // 완료 시 바로 제거하지 않고 클라이언트에서 재연결 처리
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: {}", userEmail);
            removeEmitter(userEmail);
        });
        
        emitter.onError(e -> {
            log.error("SSE 연결 오류: {}, 에러: {}", userEmail, e.getMessage());
            removeEmitter(userEmail);
        });
        
        // 맵에 저장
        EMITTERS.put(userEmail, emitter);
        
        // 초기 연결 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결되었습니다"));
            log.info("SSE 초기 이벤트 전송 완료: {}", userEmail);
        } catch (org.springframework.web.context.request.async.AsyncRequestNotUsableException e) {
            log.debug("SSE 초기 이벤트 전송 실패 (응답 이미 닫힘): {}", userEmail);
            removeEmitter(userEmail);
            // 새 emitter 생성 대신 null 반환 - 클라이언트가 재연결 처리하도록
            SseEmitter newEmitter = new SseEmitter(TIMEOUT);
            newEmitter.complete(); // 즉시 완료 처리하여 클라이언트가 재연결하도록 함
            return newEmitter;
        } catch (IOException e) {
            log.error("SSE 초기 이벤트 전송 실패: {}", e.getMessage());
            removeEmitter(userEmail);
        }
        
        return emitter;
    }
    
    /**
     * 특정 사용자에게 이벤트 전송
     */
    public static void sendToUser(String userEmail, String eventName, Object data) {
        SseEmitter emitter = EMITTERS.get(userEmail);
        if (emitter != null) {
            try {
                log.debug("SSE 이벤트 전송 시도: {} -> {}", eventName, userEmail);
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("SSE 이벤트 전송 성공: {} -> {}", eventName, userEmail);
            } catch (org.springframework.web.context.request.async.AsyncRequestNotUsableException e) {
                log.debug("SSE 이벤트 전송 실패 (응답 이미 닫힘): {} -> {}", eventName, userEmail);
                removeEmitter(userEmail);
            } catch (IOException e) {
                log.error("SSE 이벤트 전송 실패: {} -> {}, 에러: {}", eventName, userEmail, e.getMessage());
                removeEmitter(userEmail);
            } catch (Exception e) {
                log.error("SSE 이벤트 전송 중 예외 발생: {} -> {}, 에러: {}", eventName, userEmail, e.getMessage(), e);
                removeEmitter(userEmail);
            }
        } else {
            log.debug("SSE 이벤트 전송 실패 (emitter 없음): {} -> {}", eventName, userEmail);
        }
    }
    
    /**
     * 연결 제거
     */
    private static void removeEmitter(String userEmail) {
        SseEmitter emitter = EMITTERS.remove(userEmail);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("SSE 연결 제거: {}", userEmail);
            } catch (Exception e) {
                // 기타 예외는 로깅하고 무시
                log.debug("SSE 연결 제거 중 예외 (무시됨): {} - {}", userEmail, e.getMessage());
            }
        }
    }
    
    /**
     * 사용자 연결 여부 확인
     */
    public static boolean isConnected(String userEmail) {
        return EMITTERS.containsKey(userEmail);
    }
    
    /**
     * 주기적으로 ping 이벤트를 보내 연결 유지
     */
    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void sendPingToAllClients() {
        if (EMITTERS.isEmpty()) {
            return; // 연결된 클라이언트가 없으면 실행하지 않음
        }
        
        log.debug("Ping 이벤트 전송 시작 (연결된 클라이언트: {}개)", EMITTERS.size());
        
        // 제거할 클라이언트 목록
        // ConcurrentModificationException 방지를 위해 별도 목록에 담아 나중에 처리
        final java.util.List<String> clientsToRemove = new java.util.ArrayList<>();
        
        // 모든 클라이언트에 ping 이벤트 전송
        for (Map.Entry<String, SseEmitter> entry : EMITTERS.entrySet()) {
            String userEmail = entry.getKey();
            SseEmitter emitter = entry.getValue();
            
            try {
                // 전송 전에 이미 완료된 emitter인지 확인하는 로직을 추가할 수 있음
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("keep-alive"));
                log.debug("Ping 전송 성공: {}", userEmail);
            } catch (org.springframework.web.context.request.async.AsyncRequestNotUsableException e) {
                log.debug("Ping 전송 실패 (응답 이미 닫힘), 연결 제거 예정: {}", userEmail);
                clientsToRemove.add(userEmail);
            } catch (Exception e) {
                log.debug("Ping 전송 실패, 연결 제거 예정: {} - {}", userEmail, e.getMessage());
                clientsToRemove.add(userEmail);
            }
        }
        
        // 실패한 클라이언트 연결 제거
        for (String userEmail : clientsToRemove) {
            removeEmitter(userEmail);
        }
    }
}