package com.dirinekyan.eft.exception;

import java.time.Instant;

/**
 * Единый формат ошибки API.
 */
public record ErrorResponse(String errorMessage, String errorCode, Instant timestamp, String path) {
}
