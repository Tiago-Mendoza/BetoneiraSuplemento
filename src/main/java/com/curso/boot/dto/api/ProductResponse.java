package com.curso.boot.dto.api;

import java.math.BigDecimal;

/**
 * DTO de resposta da API REST para um produto.
 * Expõe apenas os campos necessários para consumidores da API,
 * isolando o domínio interno da representação pública.
 */
public record ProductResponse(
        Long id,
        String nome,
        String subtitulo,
        String peso,
        BigDecimal preco,
        String precoFormatado,
        String imagemUrl,
        boolean destaque
) {}
