package com.curso.boot.BetoneiraSuplementos.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "Informe o nome do destinatário.")
    @Size(min = 3, max = 100, message = "Informe um nome válido.")
    private String recipientName;

    @NotBlank(message = "Informe o CEP.")
    private String zipCode;

    @NotBlank(message = "Informe o logradouro.")
    private String street;

    @NotBlank(message = "Informe o número.")
    private String number;

    private String complement;

    @NotBlank(message = "Informe o bairro.")
    private String neighborhood;

    @NotBlank(message = "Informe a cidade.")
    private String city;

    @NotBlank(message = "Informe o estado.")
    @Size(min = 2, max = 30, message = "Informe um estado válido.")
    private String state;

    @NotBlank(message = "Selecione a forma de pagamento.")
    private String paymentMethod;

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
