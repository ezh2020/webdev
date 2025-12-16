package com.webdev.webdev.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知中心 WebSocket 处理器：
 * - 维护在线会话列表
 * - 提供 broadcast(type, message) 方法，向所有在线用户推送通知。
 *
 * WebSocket 消息格式约定为 JSON：
 * { "type": "ENROLL" | "DROP" | "GRADE" | "SYSTEM", "message": "...", "time": "yyyy-MM-dd HH:mm:ss" }
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 使用线程安全集合保存在线会话
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.debug("WebSocket connected, sessionId={}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.debug("WebSocket closed, sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error, sessionId={}", session.getId(), exception);
        sessions.remove(session);
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 向所有在线会话广播一条通知消息。
     *
     * @param type    通知类型：ENROLL / DROP / GRADE / SYSTEM 等
     * @param message 通知内容
     */
    public void broadcast(String type, String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type != null ? type : "SYSTEM");
        payload.put("message", message.trim());
        payload.put("time", LocalDateTime.now().format(TIME_FMT));

        final String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Failed to serialize WebSocket notification payload", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                log.warn("Failed to send WebSocket notification, sessionId={}", session.getId(), e);
            }
        }
    }
}

