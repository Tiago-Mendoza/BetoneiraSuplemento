package com.curso.boot.dto.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de resposta da API REST para um pedido.
 * Usado tanto na listagem resumida (itens = lista vazia)
 * quanto no detalhamento completo (itens = lista completa de OrderItemResponse).
 */
public record OrderResponse(
        Long id,
        String numeroPedido,
        String status,
        String formaPagamento,
        String destinatario,
        String enderecoEntrega,
        BigDecimal subtotal,
        String subtotalFormatado,
        BigDecimal frete,
        String freteFormatado,
        BigDecimal total,
        String totalFormatado,
        String dataCriacao,
        int quantidadeItens,
        List<OrderItemResponse> itens
) {}
