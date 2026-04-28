package com.curso.boot.BetoneiraSuplementos.store.service;

import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminProductForm;
import com.curso.boot.BetoneiraSuplementos.store.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ProductCatalogService {

    private static final List<Product> DEFAULT_PRODUCTS = List.of(
        new Product(
            "whey-amendoim-iso",
            "Whey",
            "Sabor Amendoim ISO",
            "900g",
            new BigDecimal("119.99"),
            "WHEYAMENDOIMISO-removebg-preview.png",
            "Whey Protein Amendoim ISO",
            false
        ),
        new Product(
            "whey-chocolate",
            "Whey",
            "Sabor Chocolate",
            "900g",
            new BigDecimal("119.99"),
            "WHEYCHOCOL-removebg-preview.png",
            "Whey Protein Chocolate",
            true
        ),
        new Product(
            "whey-coco",
            "Whey",
            "Sabor Coco",
            "900g",
            new BigDecimal("119.99"),
            "WHEYCOCO-removebg-preview.png",
            "Whey Protein Coco",
            false
        ),
        new Product(
            "whey-cookies",
            "Whey",
            "Sabor Cookies",
            "900g",
            new BigDecimal("119.99"),
            "WHEYCOOKIES-removebg-preview.png",
            "Whey Protein Cookies",
            false
        ),
        new Product(
            "whey-frutas-vermelhas",
            "Whey",
            "Sabor Frutas Vermelhas",
            "900g",
            new BigDecimal("119.99"),
            "WHEYFRUTAS-removebg-preview.png",
            "Whey Protein Frutas Vermelhas",
            false
        ),
        new Product(
            "whey-morango",
            "Whey",
            "Sabor Morango",
            "900g",
            new BigDecimal("119.99"),
            "WHEYMORANGO-removebg-preview.png",
            "Whey Protein Morango",
            false
        ),
        new Product(
            "creatina-ouro-em-po",
            "Creatina",
            "Ouro em Po",
            "300g",
            new BigDecimal("119.99"),
            "CREATINAEMPÓ-removebg-preview.png",
            "Creatina em Po",
            true
        ),
        new Product(
            "creatina-micronizada",
            "Creatina",
            "Micronizada",
            "300g",
            new BigDecimal("119.99"),
            "CREATINAMICRONIZADA-removebg-preview.png",
            "Creatina Micronizada",
            true
        ),
        new Product(
            "creatina-miro-pure",
            "Creatina",
            "Miro Pure",
            "300g",
            new BigDecimal("119.99"),
            "CREATINAMIRO-removebg-preview.png",
            "Creatina Miro",
            false
        )
    );

    private final ObjectMapper objectMapper;
    private final Path productsFilePath;
    private final TypeReference<List<Product>> productsType = new TypeReference<>() {};
    private final Object fileLock = new Object();

    public ProductCatalogService(
        ObjectMapper objectMapper,
        @Value("${app.products.file:./data/products.json}") String productsFilePath
    ) {
        this.objectMapper = objectMapper;
        this.productsFilePath = Path.of(productsFilePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initializeStore() {
        synchronized (fileLock) {
            try {
                Path parent = productsFilePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                if (Files.notExists(productsFilePath) || Files.size(productsFilePath) == 0) {
                    writeProducts(new ArrayList<>(DEFAULT_PRODUCTS));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Nao foi possivel inicializar o arquivo de produtos.", exception);
            }
        }
    }

    public List<Product> getAllProducts() {
        synchronized (fileLock) {
            return readProducts();
        }
    }

    public List<Product> getFeaturedProducts() {
        synchronized (fileLock) {
            return readProducts().stream()
                .filter(Product::isFeatured)
                .toList();
        }
    }

    public Product getRequiredProduct(String productId) {
        synchronized (fileLock) {
            return readProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado."));
        }
    }

    public Product createProduct(AdminProductForm form) {
        synchronized (fileLock) {
            List<Product> products = readProducts();
            Product product = buildProduct(generateUniqueId(products, form), form);
            products.add(product);
            writeProducts(products);
            return product;
        }
    }

    public Product updateProduct(String productId, AdminProductForm form) {
        synchronized (fileLock) {
            List<Product> products = readProducts();
            int index = findProductIndex(products, productId);
            Product updatedProduct = buildProduct(productId, form);
            products.set(index, updatedProduct);
            writeProducts(products);
            return updatedProduct;
        }
    }

    public void deleteProduct(String productId) {
        synchronized (fileLock) {
            List<Product> products = readProducts();
            boolean removed = products.removeIf(product -> product.getId().equals(productId));
            if (!removed) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado.");
            }
            writeProducts(products);
        }
    }

    public static List<Product> getDefaultCatalog() {
        return List.copyOf(DEFAULT_PRODUCTS);
    }

    private Product buildProduct(String id, AdminProductForm form) {
        String name = normalizeText(form.getName());
        String subtitle = normalizeText(form.getSubtitle());
        String weight = normalizeText(form.getWeight());
        String imageFileName = normalizeText(form.getImageFileName());
        String altText = normalizeOptionalText(form.getAltText());

        if (!StringUtils.hasText(altText)) {
            altText = String.join(" ", List.of(name, subtitle, weight)).trim();
        }

        return new Product(
            id,
            name,
            subtitle,
            weight,
            form.getPrice().setScale(2, RoundingMode.HALF_UP),
            imageFileName,
            altText,
            form.isFeatured()
        );
    }

    private int findProductIndex(List<Product> products, String productId) {
        for (int index = 0; index < products.size(); index++) {
            if (products.get(index).getId().equals(productId)) {
                return index;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado.");
    }

    private String generateUniqueId(List<Product> products, AdminProductForm form) {
        String baseSlug = slugify(normalizeText(form.getName()) + "-" + normalizeText(form.getSubtitle()));
        if (!StringUtils.hasText(baseSlug)) {
            baseSlug = "produto";
        }

        String candidate = baseSlug;
        int suffix = 2;
        while (containsProductId(products, candidate)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean containsProductId(List<Product> products, String productId) {
        return products.stream().anyMatch(product -> product.getId().equals(productId));
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
        return normalized
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");
    }

    private List<Product> readProducts() {
        try {
            if (Files.notExists(productsFilePath) || Files.size(productsFilePath) == 0) {
                return new ArrayList<>(DEFAULT_PRODUCTS);
            }

            try (InputStream inputStream = Files.newInputStream(productsFilePath)) {
                List<Product> products = objectMapper.readValue(inputStream, productsType);
                return products == null ? new ArrayList<>() : new ArrayList<>(products);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo de produtos.", exception);
        }
    }

    private void writeProducts(List<Product> products) {
        try (OutputStream outputStream = Files.newOutputStream(
            productsFilePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, products);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel salvar o arquivo de produtos.", exception);
        }
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeOptionalText(String value) {
        String normalized = normalizeText(value);
        return normalized.isBlank() ? null : normalized;
    }
}
