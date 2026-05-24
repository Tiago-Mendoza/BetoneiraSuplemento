package com.curso.boot.web.controller.api;

import com.curso.boot.domain.Order;
import com.curso.boot.domain.OrderItem;
import com.curso.boot.dto.api.OrderItemResponse;
import com.curso.boot.dto.api.OrderResponse;
import com.curso.boot.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST interna para consulta de pedidos pelo usuário autenticado.
 * Permite que futuros clientes (apps mobile, front-end SPA) consultem
 * pedidos no formato JSON com controle de propriedade por e-mail.
 *
 * Requer autenticação — protegido via SecurityConfig.
 *
 * Endpoints:
 *   GET /api/pedidos             → lista pedidos do usuário logado
 *   GET /api/pedidos/{numero}    → detalha um pedido (dono ou ADMIN)
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoApiController {

    private final OrderService orderService;

    public PedidoApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Retorna todos os pedidos do usuário autenticado.
     * Filtra por e-mail do principal — nunca expõe pedidos de outros usuários.
     * @return 200 OK com array JSON de pedidos (sem itens detalhados)
     */
    @GetMapping
    public List<OrderResponse> meusPedidos(Authentication authentication) {
        return orderService.getOrdersByCustomerEmail(authentication.getName()).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * Retorna os detalhes completos de um pedido, incluindo todos os itens.
     * Aplica controle de propriedade: apenas o dono ou ROLE_ADMIN podem acessar.
     * @param orderNumber número do pedido (ex: BTN-20260523-A3F1)
     * @return 200 OK com pedido detalhado, 403 Forbidden, ou 404 Not Found
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> detalharPedido(
            @PathVariable String orderNumber,
            Authentication authentication
    ) {
        Order order = orderService.findByOrderNumber(orderNumber).orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isOwner = authentication.getName().equalsIgnoreCase(order.getCustomerEmail());
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toDetailResponse(order));
    }

    // ── mapeamento de domínio → DTOs ────────────────────────────────────────

    /** Resumo do pedido sem itens (para listagem). */
    private OrderResponse toSummaryResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus(),
                o.getPaymentMethod(),
                o.getRecipientName(),
                safeFullAddress(o),
                o.getSubtotal(), o.getFormattedSubtotal(),
                o.getShipping(), o.getFormattedShipping(),
                o.getTotal(), o.getFormattedTotal(),
                o.getFormattedCreatedAt(),
                o.getItemsCount(),
                List.of()
        );
    }

    /** Detalhamento completo do pedido com itens. */
    private OrderResponse toDetailResponse(Order o) {
        List<OrderItemResponse> itens = o.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getStatus(),
                o.getPaymentMethod(),
                o.getRecipientName(),
                safeFullAddress(o),
                o.getSubtotal(), o.getFormattedSubtotal(),
                o.getShipping(), o.getFormattedShipping(),
                o.getTotal(), o.getFormattedTotal(),
                o.getFormattedCreatedAt(),
                o.getItemsCount(),
                itens
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductName(),
                item.getProductSubtitle(),
                item.getProductWeight(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getFormattedUnitPrice(),
                item.getTotal(),
                item.getFormattedTotal()
        );
    }

    /** Gera endereço completo de forma segura (evita NPE para pedidos sem endereço). */
    private String safeFullAddress(Order o) {
        try {
            return o.getFullAddress();
        } catch (Exception e) {
            return "";
        }
    }
}
