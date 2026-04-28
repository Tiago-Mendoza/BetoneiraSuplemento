package com.curso.boot.BetoneiraSuplementos.cart.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Integer> items = new LinkedHashMap<>();

    public Map<String, Integer> getItems() {
        return items;
    }
}
