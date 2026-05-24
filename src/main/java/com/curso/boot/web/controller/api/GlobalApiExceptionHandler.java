package com.curso.boot.web.controller.api;

import com.curso.boot.dto.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tratamento centralizado de exceções para toda a camada REST da API.
 * Escopo limitado ao pacote api/ — não interfere nos controllers MVC Thymeleaf.
 *
 * Garante que todas as respostas de erro sigam o formato padronizado ApiError,
 * facilitando o tratamento por qualquer consumidor da API.
 */
@RestControllerAdvice(basePackages = "com.curso.boot.web.controller.api")
public class GlobalApiExceptionHandler {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Trata ResponseStatusException (ex: 404 Not Found, 403 Forbidden).
     * Retorna o status HTTP correspondente com mensagem legível.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        ApiError error = new ApiError(
                ex.getStatusCode().value(),
                ex.getStatusCode().toString(),
                ex.getReason() != null ? ex.getReason() : "Requisição inválida.",
                LocalDateTime.now().format(TIMESTAMP_FMT)
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    /**
     * Trata quaisquer outras exceções não previstas.
     * Retorna 500 com mensagem genérica — não expõe detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro interno. Tente novamente mais tarde.",
                LocalDateTime.now().format(TIMESTAMP_FMT)
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
