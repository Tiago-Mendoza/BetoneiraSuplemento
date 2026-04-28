package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.BetoneiraSuplementos.cart.service.CartService;
import com.curso.boot.BetoneiraSuplementos.checkout.model.StoredOrder;
import com.curso.boot.BetoneiraSuplementos.store.model.Product;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderHistoryAndAdminIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.users.file}")
    private String usersFile;

    @Value("${app.orders.file}")
    private String ordersFile;

    @Value("${app.products.file}")
    private String productsFile;

    private Path usersFilePath;
    private Path ordersFilePath;
    private Path productsFilePath;

    @BeforeEach
    void resetFiles() throws IOException {
        usersFilePath = Path.of(usersFile).toAbsolutePath().normalize();
        ordersFilePath = Path.of(ordersFile).toAbsolutePath().normalize();
        productsFilePath = Path.of(productsFile).toAbsolutePath().normalize();

        Files.createDirectories(usersFilePath.getParent());
        Files.createDirectories(ordersFilePath.getParent());
        Files.createDirectories(productsFilePath.getParent());
        Files.writeString(usersFilePath, "[]", StandardCharsets.UTF_8);
        Files.writeString(ordersFilePath, "[]", StandardCharsets.UTF_8);
        Files.writeString(
            productsFilePath,
            objectMapper.writeValueAsString(ProductCatalogService.getDefaultCatalog()),
            StandardCharsets.UTF_8
        );
    }

    @Test
    void meusPedidosExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/meus-pedidos"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void meusPedidosMostraSomentePedidosDoUsuarioLogado() throws Exception {
        String firstOrderNumber = criarPedidoViaCheckout("cliente1@betoneira.com", "Cliente Um");
        criarPedidoViaCheckout("cliente2@betoneira.com", "Cliente Dois");

        mockMvc.perform(get("/meus-pedidos")
                .with(user("cliente1@betoneira.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(firstOrderNumber)))
            .andExpect(content().string(containsString("Cliente Um")))
            .andExpect(content().string(not(containsString("Cliente Dois"))))
            .andExpect(content().string(not(containsString("cliente2@betoneira.com"))));
    }

    @Test
    void paginaDeSucessoDoPedidoRespeitaDonoOuAdmin() throws Exception {
        String orderNumber = criarPedidoViaCheckout("cliente1@betoneira.com", "Cliente Um");

        mockMvc.perform(get("/checkout/sucesso")
                .with(user("cliente2@betoneira.com").roles("USER"))
                .param("pedido", orderNumber))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/checkout/sucesso")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .param("pedido", orderNumber))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(orderNumber)));
    }

    @Test
    void areaAdminExigeRoleAdmin() throws Exception {
        mockMvc.perform(get("/admin/pedidos")
                .with(user("cliente@betoneira.com").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCrudDePedidosFunciona() throws Exception {
        mockMvc.perform(get("/admin/pedidos/novo")
                .with(user("admin@betoneira.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Novo Pedido")));

        mockMvc.perform(post("/admin/pedidos")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("customerEmail", "manual@betoneira.com")
                .param("recipientName", "Pedido Manual")
                .param("zipCode", "04567-000")
                .param("street", "Rua Admin")
                .param("number", "500")
                .param("complement", "Sala 10")
                .param("neighborhood", "Centro")
                .param("city", "Sao Paulo")
                .param("state", "SP")
                .param("paymentMethod", "PIX")
                .param("status", "CONFIRMADO")
                .param("productId", "whey-chocolate")
                .param("quantity", "2"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/pedidos?created"));

        List<StoredOrder> createdOrders = readOrders();
        assertThat(createdOrders).hasSize(1);
        assertThat(createdOrders.get(0).getSource()).isEqualTo("ADMIN");
        assertThat(createdOrders.get(0).getCustomerEmail()).isEqualTo("manual@betoneira.com");
        assertThat(createdOrders.get(0).getItems()).hasSize(1);
        assertThat(createdOrders.get(0).getItems().get(0).getQuantity()).isEqualTo(2);

        String orderNumber = createdOrders.get(0).getOrderNumber();

        mockMvc.perform(get("/admin/pedidos/" + orderNumber)
                .with(user("admin@betoneira.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(orderNumber)));

        mockMvc.perform(post("/admin/pedidos/" + orderNumber + "/atualizar")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("customerEmail", "atualizado@betoneira.com")
                .param("recipientName", "Pedido Atualizado")
                .param("zipCode", "04567000")
                .param("street", "Rua Atualizada")
                .param("number", "999")
                .param("complement", "")
                .param("neighborhood", "Jardins")
                .param("city", "Sao Paulo")
                .param("state", "SP")
                .param("paymentMethod", "Boleto")
                .param("status", "ENVIADO"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/pedidos?updated"));

        List<StoredOrder> updatedOrders = readOrders();
        assertThat(updatedOrders).hasSize(1);
        assertThat(updatedOrders.get(0).getCustomerEmail()).isEqualTo("atualizado@betoneira.com");
        assertThat(updatedOrders.get(0).getRecipientName()).isEqualTo("Pedido Atualizado");
        assertThat(updatedOrders.get(0).getStatus()).isEqualTo("ENVIADO");
        assertThat(updatedOrders.get(0).getPaymentMethod()).isEqualTo("Boleto");

        mockMvc.perform(post("/admin/pedidos/" + orderNumber + "/remover")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/pedidos?deleted"));

        assertThat(readOrders()).isEmpty();
    }

    @Test
    void adminCrudDeProdutosFunciona() throws Exception {
        mockMvc.perform(get("/admin/produtos/novo")
                .with(user("admin@betoneira.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Novo produto")));

        mockMvc.perform(post("/admin/produtos")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("name", "Pre Treino")
                .param("subtitle", "Sabor Laranja")
                .param("weight", "300g")
                .param("price", "89.90")
                .param("imageFileName", "https://cdn.example.com/pre-treino.png")
                .param("altText", "Pre Treino Laranja")
                .param("featured", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/produtos?created"));

        List<Product> createdProducts = readProducts();
        Product createdProduct = createdProducts.stream()
            .filter(product -> product.getId().equals("pre-treino-sabor-laranja"))
            .findFirst()
            .orElseThrow();

        assertThat(createdProduct.getName()).isEqualTo("Pre Treino");
        assertThat(createdProduct.getPrice()).isEqualByComparingTo("89.90");
        assertThat(createdProduct.getImageFileName()).isEqualTo("https://cdn.example.com/pre-treino.png");
        assertThat(createdProduct.isFeatured()).isTrue();

        mockMvc.perform(get("/admin/produtos/" + createdProduct.getId())
                .with(user("admin@betoneira.com").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Editar produto")));

        mockMvc.perform(post("/admin/produtos/" + createdProduct.getId() + "/atualizar")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("name", "Pre Treino Extreme")
                .param("subtitle", "Sabor Uva")
                .param("weight", "320g")
                .param("price", "99.50")
                .param("imageFileName", "pre-treino-uva.png")
                .param("altText", "Pre Treino Uva"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/produtos?updated"));

        List<Product> updatedProducts = readProducts();
        Product updatedProduct = updatedProducts.stream()
            .filter(product -> product.getId().equals(createdProduct.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(updatedProduct.getName()).isEqualTo("Pre Treino Extreme");
        assertThat(updatedProduct.getSubtitle()).isEqualTo("Sabor Uva");
        assertThat(updatedProduct.getWeight()).isEqualTo("320g");
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo("99.50");
        assertThat(updatedProduct.getImageFileName()).isEqualTo("pre-treino-uva.png");
        assertThat(updatedProduct.isFeatured()).isFalse();

        mockMvc.perform(post("/admin/produtos/" + createdProduct.getId() + "/remover")
                .with(user("admin@betoneira.com").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/produtos?deleted"));

        assertThat(readProducts()).extracting(Product::getId).doesNotContain(createdProduct.getId());
    }

    private String criarPedidoViaCheckout(String email, String recipientName) throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);

        String redirectUrl = mockMvc.perform(post("/checkout/finalizar")
                .with(user(email).roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("recipientName", recipientName)
                .param("zipCode", "01234-567")
                .param("street", "Rua da Betoneira")
                .param("number", "123")
                .param("complement", "")
                .param("neighborhood", "Centro")
                .param("city", "Sao Paulo")
                .param("state", "SP")
                .param("paymentMethod", "PIX"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/checkout/sucesso?pedido=BTN-*"))
            .andReturn()
            .getResponse()
            .getRedirectedUrl();

        return redirectUrl.substring(redirectUrl.indexOf("pedido=") + 7);
    }

    private List<StoredOrder> readOrders() throws IOException {
        return objectMapper.readValue(
            Files.readString(ordersFilePath, StandardCharsets.UTF_8),
            new TypeReference<List<StoredOrder>>() {}
        );
    }

    private List<Product> readProducts() throws IOException {
        return objectMapper.readValue(
            Files.readString(productsFilePath, StandardCharsets.UTF_8),
            new TypeReference<List<Product>>() {}
        );
    }
}
