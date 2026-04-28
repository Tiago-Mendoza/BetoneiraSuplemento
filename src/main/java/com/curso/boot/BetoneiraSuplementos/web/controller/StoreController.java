package com.curso.boot.BetoneiraSuplementos.web.controller;

import com.curso.boot.BetoneiraSuplementos.cart.service.CartService;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StoreController {

    private final ProductCatalogService productCatalogService;
    private final CartService cartService;

    public StoreController(ProductCatalogService productCatalogService, CartService cartService) {
        this.productCatalogService = productCatalogService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredProducts", productCatalogService.getFeaturedProducts());
        return "home";
    }

    @GetMapping("/produtos")
    public String produtos(Model model) {
        model.addAttribute("products", productCatalogService.getAllProducts());
        return "produtos";
    }

    @GetMapping("/sobre-nos")
    public String sobreNos() {
        return "sobre-nos";
    }

    @GetMapping("/carrinho")
    public String carrinho(Model model, HttpSession session) {
        model.addAttribute("cart", cartService.getSummary(session));
        return "carrinho";
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionarAoCarrinho(@RequestParam String productId, HttpSession session) {
        cartService.addProduct(productId, session);
        return "redirect:/carrinho?added";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizarQuantidade(@RequestParam String productId, @RequestParam int quantity, HttpSession session) {
        cartService.updateQuantity(productId, quantity, session);
        return "redirect:/carrinho?updated";
    }

    @PostMapping("/carrinho/remover")
    public String removerDoCarrinho(@RequestParam String productId, HttpSession session) {
        cartService.removeProduct(productId, session);
        return "redirect:/carrinho?removed";
    }
}
