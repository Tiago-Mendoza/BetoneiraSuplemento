package com.curso.boot.web.controller;

import com.curso.boot.cart.CartService;
import com.curso.boot.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StoreController {

    private final ProductService ProductService;
    private final CartService cartService;

    public StoreController(ProductService ProductService, CartService cartService) {
        this.ProductService = ProductService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredProducts", ProductService.getFeaturedProducts());
        return "home";
    }

    @GetMapping("/produtos")
    public String produtos(Model model) {
        model.addAttribute("products", ProductService.getAllProducts());
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
    public String adicionarAoCarrinho(@RequestParam Long productId, HttpSession session) {
        cartService.addProduct(productId, session);
        return "redirect:/carrinho?added";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizarQuantidade(@RequestParam Long productId, @RequestParam int quantity, HttpSession session) {
        cartService.updateQuantity(productId, quantity, session);
        return "redirect:/carrinho?updated";
    }

    @PostMapping("/carrinho/remover")
    public String removerDoCarrinho(@RequestParam Long productId, HttpSession session) {
        cartService.removeProduct(productId, session);
        return "redirect:/carrinho?removed";
    }
}
