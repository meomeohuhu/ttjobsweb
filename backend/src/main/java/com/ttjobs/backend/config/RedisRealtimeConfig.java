package com.ttjobs.backend.config;

import com.ttjobs.backend.service.RealtimeRedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnProperty(name = "ttjobs.realtime.redis.enabled", havingValue = "true")
public class RedisRealtimeConfig {

    @Bean
    public ChannelTopic realtimeTopic(@Value("${ttjobs.realtime.redis.channel:ttjobs:realtime}") String channel) {
        return new ChannelTopic(channel);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RealtimeRedisSubscriber subscriber,
            ChannelTopic realtimeTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, realtimeTopic);
        return container;
    }
}
