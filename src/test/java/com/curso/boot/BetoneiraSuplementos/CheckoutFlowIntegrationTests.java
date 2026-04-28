package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.BetoneiraSuplementos.cart.model.CartSummary;
import com.curso.boot.BetoneiraSuplementos.cart.service.CartService;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CheckoutFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.orders.file}")
    private String ordersFile;

    @Value("${app.products.file}")
    private String productsFile;

    private Path ordersFilePath;
    private Path productsFilePath;

    @BeforeEach
    void resetFiles() throws IOException {
        ordersFilePath = Path.of(ordersFile).toAbsolutePath().normalize();
        productsFilePath = Path.of(productsFile).toAbsolutePath().normalize();

        Files.createDirectories(ordersFilePath.getParent());
        Files.createDirectories(productsFilePath.getParent());
        Files.writeString(ordersFilePath, "[]", StandardCharsets.UTF_8);
        Files.writeString(
            productsFilePath,
            objectMapper.writeValueAsString(ProductCatalogService.getDefaultCatalog()),
            StandardCharsets.UTF_8
        );
    }

    @Test
    void checkoutExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/checkout"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void finalizarCheckoutSalvaPedidoELimpaCarrinho() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);
        cartService.addProduct("creatina-ouro-em-po", session);

        mockMvc.perform(post("/checkout/finalizar")
                .with(user("teste@betoneira.com").roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("recipientName", "Usuário Checkout")
                .param("zipCode", "01234-567")
                .param("street", "Rua da Betoneira")
                .param("number", "123")
                .param("complement", "Apto 5")
                .param("neighborhood", "Centro")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("paymentMethod", "PIX"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/checkout/sucesso?pedido=BTN-*"));

        List<Map<String, Object>> orders = objectMapper.readValue(
            Files.readString(ordersFilePath, StandardCharsets.UTF_8),
            new TypeReference<>() {}
        );

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).get("customerEmail")).isEqualTo("teste@betoneira.com");
        assertThat(orders.get(0).get("paymentMethod")).isEqualTo("PIX");
        assertThat(orders.get(0).get("total").toString()).isEqualTo("264.98");

        CartSummary summary = cartService.getSummary(session);
        assertThat(summary.isEmpty()).isTrue();
    }

    @Test
    void sucessoRenderizaPedidoSalvo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);

        String redirectUrl = mockMvc.perform(post("/checkout/finalizar")
                .with(user("teste@betoneira.com").roles("USER"))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("recipientName", "Usuário Checkout")
                .param("zipCode", "01234567")
                .param("street", "Rua da Betoneira")
                .param("number", "123")
                .param("complement", "")
                .param("neighborhood", "Centro")
                .param("city", "São Paulo")
                .param("state", "SP")
                .param("paymentMethod", "Cartão de Crédito"))
            .andReturn()
            .getResponse()
            .getRedirectedUrl();

        mockMvc.perform(get(redirectUrl)
                .with(user("teste@betoneira.com").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Pedido confirmado")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuário Checkout")));
    }
}
