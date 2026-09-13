package com.makaut.campussense.exception;

public class LlmRateLimitException extends RuntimeException {
    public LlmRateLimitException(String message) {
        super(message);
    }
}
