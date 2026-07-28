package br.com.copadasautoras.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiError> montar(HttpStatus status, String mensagem, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(mensagem)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    // =========================
    // 🔒 ACESSO NEGADO (403)
    // =========================
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDenied(
            AuthorizationDeniedException ex, HttpServletRequest request) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado", request);
    }

    // =========================
    // ⚠️ REGRAS DE NEGÓCIO (mensagens que você lançou de propósito)
    // =========================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        String message = ex.getMessage();

        // Erro inesperado sem mensagem: não mostra "null" pra autora.
        if (message == null || message.isBlank()) {
            log.error("RuntimeException sem mensagem em {}", request.getRequestURI(), ex);
            return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ocorreu um erro inesperado. Tente novamente ou fale com a equipe.", request);
        }

        String lower = message.toLowerCase();
        HttpStatus status;
        if (lower.contains("não encontrado")) {
            status = HttpStatus.NOT_FOUND;
        } else if (lower.contains("acesso negado")) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return montar(status, message, request);
    }

    // =========================
    // 📋 VALIDAÇÃO DE CAMPOS (@Valid) — antes escapava sem corpo
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" "));
        if (mensagem.isBlank()) mensagem = "Há campos obrigatórios não preenchidos.";
        return montar(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    // =========================
    // 📎 PARTE DO MULTIPART FALTANDO (ex.: o arquivo não veio)
    // =========================
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleParteFaltando(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Faltou uma parte do envio (provavelmente o arquivo). Anexe o PDF e tente de novo.", request);
    }

    // =========================
    // 📦 CORPO ILEGÍVEL / JSON malformado
    // =========================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleCorpoIlegivel(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST,
                "Não foi possível ler os dados enviados. Recarregue a página e tente novamente.", request);
    }

    // =========================
    // ⬆️ ARQUIVO ACIMA DO LIMITE
    // =========================
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleArquivoGrande(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return montar(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo ultrapassa o limite (10 MB por PDF, 20 MB no total). Comprima e tente de novo.", request);
    }

    // =========================
    // 💥 QUALQUER OUTRA COISA INESPERADA — loga de verdade, não vaza tripa
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInesperado(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente ou fale com a equipe.", request);
    }
}