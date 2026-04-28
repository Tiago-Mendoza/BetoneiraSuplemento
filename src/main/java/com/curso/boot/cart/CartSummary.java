package com.curso.boot.cart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartSummary {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final List<CartLine> items;
    private final int totalItems;
    private final BigDecimal subtotal;
    private final BigDecimal shipping;
    private final BigDecimal total;

    public CartSummary(List<CartLine> items, int totalItems, BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {
        this.items = items;
        this.totalItems = totalItems;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.total = total;
    }

    public List<CartLine> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShipping() {
        return shipping;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getFormattedSubtotal() {
        return formatCurrency(subtotal);
    }

    public String getFormattedShipping() {
        return formatCurrency(shipping);
    }

    public String getFormattedTotal() {
        return formatCurrency(total);
    }

    public String getItemsLabel() {
        return totalItems == 1 ? "1 item" : totalItems + " itens";
    }

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value).replace('\u00A0', ' ');
    }
}
