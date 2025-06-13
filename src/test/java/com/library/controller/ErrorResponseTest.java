package com.library.controller;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void builder_shouldCreateErrorResponse_whenAllFieldsProvided() {
        //given
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> validationErrors = Map.of("field", "error");

        //when
        ErrorResponse result = ErrorResponse.builder()
                .timestamp(timestamp)
                .status(400)
                .error("Bad Request")
                .message("Test error")
                .validationErrors(validationErrors)
                .build();

        //then
        assertThat(result.getTimestamp()).isEqualTo(timestamp);
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getError()).isEqualTo("Bad Request");
        assertThat(result.getMessage()).isEqualTo("Test error");
        assertThat(result.getValidationErrors()).isEqualTo(validationErrors);
    }
}