package com.curso.boot.web.controller;

import com.curso.boot.service.OrderService;
import com.curso.boot.service.ProductService;
import com.curso.boot.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminDashboardController(
        ProductService productService,
        OrderService orderService,
        UserService userService
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        long totalProdutos = productService.getAllProducts().size();
        long totalPedidos = orderService.findAll().size();
        long totalUsuarios = userService.findAll().size();

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("recentOrders", orderService.findAll().stream().limit(5).toList());

        return "admin-dashboard";
    }
}
