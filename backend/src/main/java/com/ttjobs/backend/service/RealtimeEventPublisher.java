package com.ttjobs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RealtimeEventPublisher {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${ttjobs.realtime.redis.channel:ttjobs:realtime}")
    private String channel;

    @Value("${ttjobs.realtime.redis.enabled:false}")
    private boolean redisEnabled;

    public void publish(String destination, Object payload) {
        if (destination == null || destination.isBlank() || payload == null) {
            return;
        }

        if (redisEnabled && stringRedisTemplate != null && publishToRedis(destination, payload)) {
            return;
        }
        messagingTemplate.convertAndSend(destination, payload);
    }

    private boolean publishToRedis(String destination, Object payload) {
        try {
            String event = objectMapper.writeValueAsString(Map.of(
                    "destination", destination,
                    "payload", payload
            ));
            stringRedisTemplate.convertAndSend(channel, event);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
