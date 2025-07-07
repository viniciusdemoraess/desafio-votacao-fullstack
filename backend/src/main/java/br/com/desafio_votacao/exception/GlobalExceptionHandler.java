package br.com.desafio_votacao.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções para a API
 * Provê respostas consistentes e informativas para todas as exceções lançadas
 */
@Configuration
@Order(-2)  // Alta prioridade para capturar exceções antes de outros handlers
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        
        // Define o conteúdo padrão da resposta de erro
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("path", exchange.getRequest().getPath().value());
        
        HttpStatus status;

        if (ex instanceof UnableToVoteException) {
            UnableToVoteException utve = (UnableToVoteException) ex;
            exchange.getResponse().setStatusCode(utve.getStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String errorJson = "{\"status\":\"UNABLE_TO_VOTE\"}";
            DataBuffer dataBuffer = bufferFactory.wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }
        
        if (ex instanceof WebExchangeBindException) {
            WebExchangeBindException validationEx = (WebExchangeBindException) ex;
            status = HttpStatus.BAD_REQUEST;
            
            Map<String, String> validationErrors = new HashMap<>();
            for (FieldError fieldError : validationEx.getBindingResult().getFieldErrors()) {
                validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            
            errorDetails.put("message", "Erro de validação nos campos");
            errorDetails.put("status", status.value());
            errorDetails.put("error", status.getReasonPhrase());
            errorDetails.put("validationErrors", validationErrors);
            
            logger.warn("Erro de validação: {}", validationErrors);
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            errorDetails.put("message", responseStatusException.getReason());
            errorDetails.put("status", status.value());
            errorDetails.put("error", status.getReasonPhrase());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorDetails.put("message", ex.getMessage());
            errorDetails.put("status", status.value());
            errorDetails.put("error", status.getReasonPhrase());
        }
        
        logger.error("Erro na requisição: {} - Status: {}", ex.getMessage(), status);
        
        String errorJson = buildErrorJson(errorDetails);
        
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        DataBuffer dataBuffer = bufferFactory.wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
    
    private String buildErrorJson(Map<String, Object> errorDetails) {
        StringBuilder builder = new StringBuilder("{");
        
        errorDetails.forEach((key, value) -> {
            builder.append("\"")
                   .append(key)
                   .append("\": ");
            
            if (value instanceof Number) {
                builder.append(value);
            } else if (value instanceof Map) {
                
                Map<?, ?> mapValue = (Map<?, ?>) value;
                builder.append("{");
                
                mapValue.forEach((mapKey, mapVal) -> {
                    builder.append("\"")
                           .append(mapKey)
                           .append("\": \"")
                           .append(mapVal)
                           .append("\", ");
                });
                
                // Remove a última vírgula se houver entradas no mapa
                if (!mapValue.isEmpty()) {
                    builder.setLength(builder.length() - 2);
                }
                
                builder.append("}");
            } else {
                builder.append("\"")
                       .append(value)
                       .append("\"");
            }
            builder.append(", ");
        });
        
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 2); // Remove a última vírgula e espaço
        }
        
        builder.append("}");
        return builder.toString();
    }
}
