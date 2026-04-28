package com.curso.boot.BetoneiraSuplementos.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Product {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final String id;
    private final String name;
    private final String subtitle;
    private final String weight;
    private final BigDecimal price;
    private final String imageFileName;
    private final String altText;
    private final boolean featured;

    @JsonCreator
    public Product(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("subtitle") String subtitle,
        @JsonProperty("weight") String weight,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("imageFileName") String imageFileName,
        @JsonProperty("altText") String altText,
        @JsonProperty("featured") boolean featured
    ) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.weight = weight;
        this.price = price;
        this.imageFileName = imageFileName;
        this.altText = altText;
        this.featured = featured;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getWeight() {
        return weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getAltText() {
        return altText;
    }

    public boolean isFeatured() {
        return featured;
    }

    @JsonIgnore
    public String getFormattedPrice() {
        return formatCurrency(price);
    }

    @JsonIgnore
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

    @JsonIgnore
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

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value).replace('\u00A0', ' ');
    }
}
