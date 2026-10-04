package com.example.demo.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationMemoryService {

    private final RedisTemplate<String, String> redisTemplate;

    public ConversationMemoryService(
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public List<String> getHistory(String conversationId) {

        String key = "conversation:" + conversationId;

        List<String> history = redisTemplate.opsForList()
                .range(key, 0, -1);

        return history != null
                ? history
                : new ArrayList<>();
    }

    public void addMessage(String conversationId, String message) {

        String key = "conversation:" + conversationId;

        redisTemplate.opsForList()
                .rightPush(key, message);
    }
}