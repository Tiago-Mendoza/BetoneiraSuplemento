package com.curso.boot.BetoneiraSuplementos.web.controller;

import com.curso.boot.BetoneiraSuplementos.auth.model.StoredUser;
import com.curso.boot.BetoneiraSuplementos.auth.service.JsonUserService;
import com.curso.boot.BetoneiraSuplementos.cart.model.CartSummary;
import com.curso.boot.BetoneiraSuplementos.cart.service.CartService;
import com.curso.boot.BetoneiraSuplementos.checkout.dto.CheckoutForm;
import com.curso.boot.BetoneiraSuplementos.checkout.model.StoredOrder;
import com.curso.boot.BetoneiraSuplementos.checkout.service.JsonOrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final JsonOrderService jsonOrderService;
    private final JsonUserService jsonUserService;

    public CheckoutController(
        CartService cartService,
        JsonOrderService jsonOrderService,
        JsonUserService jsonUserService
    ) {
        this.cartService = cartService;
        this.jsonOrderService = jsonOrderService;
        this.jsonUserService = jsonUserService;
    }

    @ModelAttribute("checkoutForm")
    public CheckoutForm checkoutForm() {
        return new CheckoutForm();
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, Authentication authentication) {
        CartSummary cartSummary = cartService.getSummary(session);
        if (cartSummary.isEmpty()) {
            return "redirect:/carrinho?empty";
        }

        CheckoutForm checkoutForm = (CheckoutForm) model.getAttribute("checkoutForm");
        if (checkoutForm == null) {
            checkoutForm = checkoutForm();
            model.addAttribute("checkoutForm", checkoutForm);
        }

        if (checkoutForm.getRecipientName() == null || checkoutForm.getRecipientName().isBlank()) {
            prefillForm(checkoutForm, authentication);
        }

        model.addAttribute("cart", cartSummary);
        model.addAttribute("customerEmail", authentication.getName());
        return "checkout";
    }

    @PostMapping("/checkout/finalizar")
    public String finalizarCheckout(
        @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
        BindingResult bindingResult,
        HttpSession session,
        Authentication authentication,
        Model model
    ) {
        CartSummary cartSummary = cartService.getSummary(session);
        if (cartSummary.isEmpty()) {
            return "redirect:/carrinho?empty";
        }

        validateCheckoutData(checkoutForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cartSummary);
            model.addAttribute("customerEmail", authentication.getName());
            return "checkout";
        }

        StoredOrder order = jsonOrderService.createOrder(authentication.getName(), checkoutForm, cartSummary);
        cartService.clear(session);
        return "redirect:/checkout/sucesso?pedido=" + order.getOrderNumber();
    }

    @GetMapping("/checkout/sucesso")
    public String sucesso(@RequestParam("pedido") String orderNumber, Authentication authentication, Model model) {
        StoredOrder order = jsonOrderService.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Pedido nao encontrado."));

        boolean isOwner = authentication.getName().equalsIgnoreCase(order.getCustomerEmail());
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(FORBIDDEN, "Voce nao pode acessar este pedido.");
        }

        model.addAttribute("order", order);
        return "checkout-sucesso";
    }

    private void prefillForm(CheckoutForm checkoutForm, Authentication authentication) {
        Optional<StoredUser> storedUser = jsonUserService.findByEmail(authentication.getName());
        storedUser.ifPresent(user -> checkoutForm.setRecipientName(user.getNome()));
    }

    private void validateCheckoutData(CheckoutForm checkoutForm, BindingResult bindingResult) {
        String zipCode = digitsOnly(checkoutForm.getZipCode());
        if (!zipCode.isBlank() && zipCode.length() != 8) {
            bindingResult.rejectValue("zipCode", "invalid", "Informe um CEP com 8 digitos.");
        }

        String state = checkoutForm.getState() == null ? "" : checkoutForm.getState().trim();
        if (!state.isBlank() && state.length() < 2) {
            bindingResult.rejectValue("state", "invalid", "Informe um estado valido.");
        }
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
