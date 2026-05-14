package com.ttjobs.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RealtimeRedisSubscriber implements MessageListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String raw = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode event = objectMapper.readTree(raw);
            String destination = event.path("destination").asText("");
            JsonNode payload = event.path("payload");
            if (!destination.isBlank() && !payload.isMissingNode() && !payload.isNull()) {
                messagingTemplate.convertAndSend(destination, payload);
            }
        } catch (Exception ex) {
            // Bo qua event loi; REST API van la nguon du lieu chinh.
        }
    }
}
