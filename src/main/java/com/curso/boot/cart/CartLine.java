package com.curso.boot.cart;

import com.curso.boot.domain.Product;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CartLine {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final Product product;
    private final int quantity;
    private final BigDecimal lineTotal;

    public CartLine(Product product, int quantity, BigDecimal lineTotal) {
        this.product = product;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getFormattedLineTotal() {
        return NumberFormat.getCurrencyInstance(PT_BR).format(lineTotal).replace('\u00A0', ' ');
    }
}
