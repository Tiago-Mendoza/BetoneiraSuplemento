package com.curso.boot.BetoneiraSuplementos.checkout.service;

import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminOrderCreateForm;
import com.curso.boot.BetoneiraSuplementos.admin.dto.AdminOrderUpdateForm;
import com.curso.boot.BetoneiraSuplementos.cart.model.CartLine;
import com.curso.boot.BetoneiraSuplementos.cart.model.CartSummary;
import com.curso.boot.BetoneiraSuplementos.checkout.dto.CheckoutForm;
import com.curso.boot.BetoneiraSuplementos.checkout.model.StoredOrder;
import com.curso.boot.BetoneiraSuplementos.checkout.model.StoredOrderItem;
import com.curso.boot.BetoneiraSuplementos.store.model.Product;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class JsonOrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String CHECKOUT_SOURCE = "CHECKOUT";
    private static final String ADMIN_SOURCE = "ADMIN";

    private final ObjectMapper objectMapper;
    private final Path ordersFilePath;
    private final ProductCatalogService productCatalogService;
    private final TypeReference<List<StoredOrder>> ordersType = new TypeReference<>() {};
    private final Object fileLock = new Object();

    public JsonOrderService(
        ObjectMapper objectMapper,
        ProductCatalogService productCatalogService,
        @Value("${app.orders.file:./data/orders.json}") String ordersFilePath
    ) {
        this.objectMapper = objectMapper;
        this.productCatalogService = productCatalogService;
        this.ordersFilePath = Path.of(ordersFilePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initializeStore() {
        synchronized (fileLock) {
            try {
                Path parent = ordersFilePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                if (Files.notExists(ordersFilePath) || Files.size(ordersFilePath) == 0) {
                    writeOrders(new ArrayList<>());
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Não foi possível inicializar o arquivo de pedidos.", exception);
            }
        }
    }

    public StoredOrder createOrder(String customerEmail, CheckoutForm form, CartSummary cartSummary) {
        synchronized (fileLock) {
            List<StoredOrder> orders = readOrders();
            StoredOrder order = buildOrder(customerEmail, form, cartSummary);
            orders.add(order);
            writeOrders(orders);
            return order;
        }
    }

    public StoredOrder createAdminOrder(AdminOrderCreateForm form) {
        synchronized (fileLock) {
            List<StoredOrder> orders = readOrders();
            Product product = productCatalogService.getRequiredProduct(form.getProductId());
            StoredOrder order = buildAdminOrder(form, product);
            orders.add(order);
            writeOrders(orders);
            return order;
        }
    }

    public Optional<StoredOrder> findByOrderNumber(String orderNumber) {
        synchronized (fileLock) {
            return readOrders().stream()
                .filter(order -> orderNumber.equals(order.getOrderNumber()))
                .findFirst();
        }
    }

    public StoredOrder getRequiredByOrderNumber(String orderNumber) {
        return findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Pedido não encontrado."));
    }

    public List<StoredOrder> findByCustomerEmail(String customerEmail) {
        synchronized (fileLock) {
            String normalizedEmail = normalizeEmail(customerEmail);
            return readOrders().stream()
                .filter(order -> normalizedEmail.equals(order.getCustomerEmail()))
                .sorted(Comparator.comparing(StoredOrder::getCreatedAt).reversed())
                .toList();
        }
    }

    public List<StoredOrder> findAll() {
        synchronized (fileLock) {
            return readOrders().stream()
                .sorted(Comparator.comparing(StoredOrder::getCreatedAt).reversed())
                .toList();
        }
    }

    public StoredOrder updateOrder(String orderNumber, AdminOrderUpdateForm form) {
        synchronized (fileLock) {
            List<StoredOrder> orders = readOrders();
            StoredOrder order = orders.stream()
                .filter(existing -> orderNumber.equals(existing.getOrderNumber()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Pedido não encontrado."));

            order.setCustomerEmail(normalizeEmail(form.getCustomerEmail()));
            order.setRecipientName(normalizeText(form.getRecipientName()));
            order.setZipCode(digitsOnly(form.getZipCode()));
            order.setStreet(normalizeText(form.getStreet()));
            order.setNumber(normalizeText(form.getNumber()));
            order.setComplement(normalizeOptionalText(form.getComplement()));
            order.setNeighborhood(normalizeText(form.getNeighborhood()));
            order.setCity(normalizeText(form.getCity()));
            order.setState(normalizeText(form.getState()).toUpperCase(Locale.ROOT));
            order.setPaymentMethod(form.getPaymentMethod());
            order.setStatus(form.getStatus());

            writeOrders(orders);
            return order;
        }
    }

    public void deleteOrder(String orderNumber) {
        synchronized (fileLock) {
            List<StoredOrder> orders = readOrders();
            boolean removed = orders.removeIf(order -> orderNumber.equals(order.getOrderNumber()));
            if (!removed) {
                throw new ResponseStatusException(NOT_FOUND, "Pedido não encontrado.");
            }
            writeOrders(orders);
        }
    }

    private StoredOrder buildOrder(String customerEmail, CheckoutForm form, CartSummary cartSummary) {
        LocalDateTime createdAt = LocalDateTime.now();

        StoredOrder order = new StoredOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNumber(generateOrderNumber(createdAt));
        order.setCustomerEmail(normalizeEmail(customerEmail));
        order.setRecipientName(normalizeText(form.getRecipientName()));
        order.setZipCode(digitsOnly(form.getZipCode()));
        order.setStreet(normalizeText(form.getStreet()));
        order.setNumber(normalizeText(form.getNumber()));
        order.setComplement(normalizeOptionalText(form.getComplement()));
        order.setNeighborhood(normalizeText(form.getNeighborhood()));
        order.setCity(normalizeText(form.getCity()));
        order.setState(normalizeText(form.getState()).toUpperCase(Locale.ROOT));
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus("CONFIRMADO");
        order.setSource(CHECKOUT_SOURCE);
        order.setCreatedAt(createdAt);
        order.setSubtotal(cartSummary.getSubtotal());
        order.setShipping(cartSummary.getShipping());
        order.setTotal(cartSummary.getTotal());
        order.setItems(cartSummary.getItems().stream().map(this::mapItem).toList());
        return order;
    }

    private StoredOrder buildAdminOrder(AdminOrderCreateForm form, Product product) {
        LocalDateTime createdAt = LocalDateTime.now();
        int quantity = form.getQuantity() == null ? 1 : form.getQuantity();

        StoredOrderItem item = new StoredOrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductSubtitle(product.getSubtitle());
        item.setProductWeight(product.getWeight());
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setLineTotal(product.getPrice().multiply(java.math.BigDecimal.valueOf(quantity)));

        StoredOrder order = new StoredOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNumber(generateOrderNumber(createdAt));
        order.setCustomerEmail(normalizeEmail(form.getCustomerEmail()));
        order.setRecipientName(normalizeText(form.getRecipientName()));
        order.setZipCode(digitsOnly(form.getZipCode()));
        order.setStreet(normalizeText(form.getStreet()));
        order.setNumber(normalizeText(form.getNumber()));
        order.setComplement(normalizeOptionalText(form.getComplement()));
        order.setNeighborhood(normalizeText(form.getNeighborhood()));
        order.setCity(normalizeText(form.getCity()));
        order.setState(normalizeText(form.getState()).toUpperCase(Locale.ROOT));
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(form.getStatus());
        order.setSource(ADMIN_SOURCE);
        order.setCreatedAt(createdAt);
        order.setSubtotal(item.getLineTotal());
        order.setShipping(java.math.BigDecimal.valueOf(25.00));
        order.setTotal(order.getSubtotal().add(order.getShipping()));
        order.setItems(List.of(item));
        return order;
    }

    private StoredOrderItem mapItem(CartLine line) {
        StoredOrderItem item = new StoredOrderItem();
        item.setProductId(line.getProduct().getId());
        item.setProductName(line.getProduct().getName());
        item.setProductSubtitle(line.getProduct().getSubtitle());
        item.setProductWeight(line.getProduct().getWeight());
        item.setQuantity(line.getQuantity());
        item.setUnitPrice(line.getProduct().getPrice());
        item.setLineTotal(line.getLineTotal());
        return item;
    }

    private String generateOrderNumber(LocalDateTime createdAt) {
        return "BTN-" + createdAt.format(ORDER_NUMBER_DATE) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private List<StoredOrder> readOrders() {
        try {
            if (Files.notExists(ordersFilePath) || Files.size(ordersFilePath) == 0) {
                return new ArrayList<>();
            }

            try (InputStream inputStream = Files.newInputStream(ordersFilePath)) {
                List<StoredOrder> orders = objectMapper.readValue(inputStream, ordersType);
                return orders == null ? new ArrayList<>() : new ArrayList<>(orders);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o arquivo de pedidos.", exception);
        }
    }

    private void writeOrders(List<StoredOrder> orders) {
        try (OutputStream outputStream = Files.newOutputStream(
            ordersFilePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, orders);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar o arquivo de pedidos.", exception);
        }
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeOptionalText(String value) {
        String normalized = normalizeText(value);
        return normalized.isBlank() ? null : normalized;
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
