package com.curso.boot.BetoneiraSuplementos.cart.service;

import com.curso.boot.BetoneiraSuplementos.cart.model.CartLine;
import com.curso.boot.BetoneiraSuplementos.cart.model.CartSummary;
import com.curso.boot.BetoneiraSuplementos.cart.model.SessionCart;
import com.curso.boot.BetoneiraSuplementos.store.model.Product;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String SESSION_CART_KEY = "shoppingCart";
    private static final BigDecimal SHIPPING_COST = new BigDecimal("25.00");

    private final ProductCatalogService productCatalogService;

    public CartService(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    public void addProduct(String productId, HttpSession session) {
        Product product = productCatalogService.getRequiredProduct(productId);
        SessionCart cart = getOrCreateCart(session);
        cart.getItems().merge(product.getId(), 1, Integer::sum);
    }

    public void updateQuantity(String productId, int quantity, HttpSession session) {
        Product product = productCatalogService.getRequiredProduct(productId);
        SessionCart cart = getOrCreateCart(session);

        if (quantity <= 0) {
            cart.getItems().remove(product.getId());
        } else {
            cart.getItems().put(product.getId(), quantity);
        }

        cleanupIfEmpty(session, cart);
    }

    public void removeProduct(String productId, HttpSession session) {
        Product product = productCatalogService.getRequiredProduct(productId);
        SessionCart cart = getOrCreateCart(session);
        cart.getItems().remove(product.getId());
        cleanupIfEmpty(session, cart);
    }

    public CartSummary getSummary(HttpSession session) {
        SessionCart cart = getCart(session);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (Product product : productCatalogService.getAllProducts()) {
            Integer quantity = cart.getItems().get(product.getId());
            if (quantity == null || quantity <= 0) {
                continue;
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            lines.add(new CartLine(product, quantity, lineTotal));
            subtotal = subtotal.add(lineTotal);
            totalItems += quantity;
        }

        BigDecimal shipping = lines.isEmpty() ? BigDecimal.ZERO : SHIPPING_COST;
        BigDecimal total = subtotal.add(shipping);

        return new CartSummary(lines, totalItems, subtotal, shipping, total);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_CART_KEY);
    }

    private SessionCart getOrCreateCart(HttpSession session) {
        Object cartObject = session.getAttribute(SESSION_CART_KEY);
        if (cartObject instanceof SessionCart cart) {
            return cart;
        }

        SessionCart cart = new SessionCart();
        session.setAttribute(SESSION_CART_KEY, cart);
        return cart;
    }

    private SessionCart getCart(HttpSession session) {
        Object cartObject = session.getAttribute(SESSION_CART_KEY);
        if (cartObject instanceof SessionCart cart) {
            return cart;
        }
        return new SessionCart();
    }

    private void cleanupIfEmpty(HttpSession session, SessionCart cart) {
        if (cart.getItems().isEmpty()) {
            session.removeAttribute(SESSION_CART_KEY);
        } else {
            session.setAttribute(SESSION_CART_KEY, cart);
        }
    }
}
