package com.learn.notifiy.error;

import java.util.Map;

public record AppError(
        int status,
        String message,
        Map<String, String> errors,
        long timestamp
) {
}
