package com.curso.boot.BetoneiraSuplementos.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AdminProductForm {

    @NotBlank(message = "Informe o nome do produto.")
    @Size(max = 80, message = "Use ate 80 caracteres para o nome.")
    private String name;

    @NotBlank(message = "Informe o subtitulo do produto.")
    @Size(max = 120, message = "Use ate 120 caracteres para o subtitulo.")
    private String subtitle;

    @NotBlank(message = "Informe o peso ou volume do produto.")
    @Size(max = 40, message = "Use ate 40 caracteres para o peso.")
    private String weight;

    @NotNull(message = "Informe o preco do produto.")
    @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "Informe um preco valido com ate 2 casas decimais.")
    private BigDecimal price;

    @NotBlank(message = "Informe a imagem do produto.")
    @Size(max = 255, message = "Use ate 255 caracteres para a imagem.")
    private String imageFileName;

    @Size(max = 255, message = "Use ate 255 caracteres para o texto alternativo.")
    private String altText;

    private boolean featured;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
