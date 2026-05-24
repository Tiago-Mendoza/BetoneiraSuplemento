package com.curso.boot.web.controller.api;

import com.curso.boot.domain.Product;
import com.curso.boot.dto.api.ProductResponse;
import com.curso.boot.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST interna para exposição do catálogo de produtos.
 * Permite que futuros clientes (apps mobile, front-end SPA, integrações B2B)
 * consumam o catálogo de produtos no formato JSON.
 *
 * Acesso público — não requer autenticação.
 *
 * Endpoints:
 *   GET /api/produtos           → lista todos os produtos
 *   GET /api/produtos/destaque  → lista apenas produtos em destaque
 *   GET /api/produtos/{id}      → detalha um produto por ID
 */
@RestController
@RequestMapping("/api/produtos")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lista todos os produtos do catálogo.
     * @return 200 OK com array JSON de produtos
     */
    @GetMapping
    public List<ProductResponse> listarTodos() {
        return productService.getAllProducts().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lista apenas os produtos marcados como destaque (featured = true).
     * Útil para widgets de vitrine em outros sistemas.
     * @return 200 OK com array JSON de produtos em destaque
     */
    @GetMapping("/destaque")
    public List<ProductResponse> listarDestaques() {
        return productService.getFeaturedProducts().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retorna um produto específico pelo seu ID.
     * @param id ID do produto
     * @return 200 OK com o produto, ou 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> buscarPorId(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(product));
    }

    // ── mapeamento de domínio → DTO ─────────────────────────────────────────
    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSubtitle(),
                p.getWeight(),
                p.getPrice(),
                p.getFormattedPrice(),
                p.getImageUrl(),
                p.isFeatured()
        );
    }
}
