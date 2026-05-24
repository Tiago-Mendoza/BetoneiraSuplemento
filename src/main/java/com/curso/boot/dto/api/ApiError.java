package com.curso.boot.dto.api;

/**
 * Estrutura padronizada de resposta de erro da API REST.
 * Todas as exceções tratadas pelo GlobalApiExceptionHandler
 * retornam este formato, garantindo consistência para os consumidores da API.
 */
public record ApiError(
        int status,
        String erro,
        String mensagem,
        String timestamp
) {}
