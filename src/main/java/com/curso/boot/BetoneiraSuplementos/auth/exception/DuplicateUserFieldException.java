package com.curso.boot.BetoneiraSuplementos.auth.exception;

public class DuplicateUserFieldException extends RuntimeException {

    private final String field;

    public DuplicateUserFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
