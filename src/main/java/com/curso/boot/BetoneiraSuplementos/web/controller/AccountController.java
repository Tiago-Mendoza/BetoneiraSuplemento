package com.curso.boot.BetoneiraSuplementos.web.controller;

import com.curso.boot.BetoneiraSuplementos.checkout.service.JsonOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    private final JsonOrderService jsonOrderService;

    public AccountController(JsonOrderService jsonOrderService) {
        this.jsonOrderService = jsonOrderService;
    }

    @GetMapping("/meus-pedidos")
    public String meusPedidos(Authentication authentication, Model model) {
        model.addAttribute("orders", jsonOrderService.findByCustomerEmail(authentication.getName()));
        model.addAttribute("customerEmail", authentication.getName());
        return "meus-pedidos";
    }
}
