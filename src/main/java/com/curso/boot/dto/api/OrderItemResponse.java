package com.curso.boot.dto.api;

import java.math.BigDecimal;

/**
 * DTO de resposta da API REST para um item de pedido.
 * Armazena snapshot do produto no momento da compra,
 * garantindo integridade histórica mesmo com alterações futuras no catálogo.
 */
public record OrderItemResponse(
        String produtoNome,
        String produtoSubtitulo,
        String produtoPeso,
        int quantidade,
        BigDecimal precoUnitario,
        String precoUnitarioFormatado,
        BigDecimal totalLinha,
        String totalLinhaFormatado
) {}
