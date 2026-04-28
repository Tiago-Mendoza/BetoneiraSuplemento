package com.curso.boot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.web.util.UriUtils;

@Entity
@Table(name = "products")
public class Product extends AbstractEntity<Long> {

    @Column(nullable = false)
    private String name;

    private String subtitle;

    private String weight;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "image_file_name")
    private String imageFileName;

    @Column(name = "alt_text")
    private String altText;

    private boolean featured;

    public Product() {
    }

    public Product(String name, String subtitle, String weight, BigDecimal price, String imageFileName, String altText, boolean featured) {
        this.name = name;
        this.subtitle = subtitle;
        this.weight = weight;
        this.price = price;
        this.imageFileName = imageFileName;
        this.altText = altText;
        this.featured = featured;
    }

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

    public String getFormattedPrice() {
        Locale PT_BR = Locale.forLanguageTag("pt-BR");
        return NumberFormat.getCurrencyInstance(PT_BR).format(price).replace('\u00A0', ' ');
    }

    public String getDisplayDescription() {
        List<String> parts = new ArrayList<>();
        if (subtitle != null && !subtitle.isBlank()) {
            parts.add(subtitle);
        }
        if (weight != null && !weight.isBlank()) {
            parts.add(weight);
        }
        return String.join(" - ", parts);
    }

    public String getImageUrl() {
        if (imageFileName == null || imageFileName.isBlank()) {
            return "/image/favicon.png";
        }
        if (imageFileName.startsWith("http://") || imageFileName.startsWith("https://") || imageFileName.startsWith("/")) {
            return imageFileName;
        }
        if (imageFileName.startsWith("image/")) {
            return "/" + UriUtils.encodePath(imageFileName, StandardCharsets.UTF_8);
        }
        return "/image/" + UriUtils.encodePathSegment(imageFileName, StandardCharsets.UTF_8);
    }
}
