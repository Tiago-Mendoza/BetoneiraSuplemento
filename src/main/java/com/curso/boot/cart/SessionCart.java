package com.curso.boot.cart;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Long, Integer> items = new LinkedHashMap<>();

    public Map<Long, Integer> getItems() {
        return items;
    }
}

