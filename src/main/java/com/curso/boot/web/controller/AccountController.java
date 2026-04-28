package com.curso.boot.web.controller;

import com.curso.boot.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    private final OrderService OrderService;

    public AccountController(OrderService OrderService) {
        this.OrderService = OrderService;
    }

    @GetMapping("/meus-pedidos")
    public String meusPedidos(Authentication authentication, Model model) {
        model.addAttribute("orders", OrderService.getOrdersByCustomerEmail(authentication.getName()));
        model.addAttribute("customerEmail", authentication.getName());
        return "meus-pedidos";
    }
}
