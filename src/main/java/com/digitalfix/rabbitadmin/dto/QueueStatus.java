package com.digitalfix.rabbitadmin.dto;

public record QueueStatus(
        String name,
        boolean dlq,
        long messageCount,
        long consumerCount
) {}
