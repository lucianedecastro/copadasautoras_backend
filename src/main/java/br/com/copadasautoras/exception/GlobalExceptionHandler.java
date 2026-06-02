package br.com.copadasautoras.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // 🔒 ACESSO NEGADO (403)
    // =========================
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .erro("Acesso negado")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // =========================
    // ⚠️ REGRAS DE NEGÓCIO
    // =========================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        HttpStatus status;
        String message = ex.getMessage();

        if (message != null
                && message.toLowerCase().contains("não encontrado")) {

            status = HttpStatus.NOT_FOUND;

        } else if (message != null
                && message.toLowerCase().contains("acesso negado")) {

            status = HttpStatus.FORBIDDEN;

        } else {

            status = HttpStatus.BAD_REQUEST;
        }

        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(status)
                .body(error);
    }
}

