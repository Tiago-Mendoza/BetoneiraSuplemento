package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.BetoneiraSuplementos.cart.model.CartSummary;
import com.curso.boot.BetoneiraSuplementos.cart.service.CartService;
import com.curso.boot.BetoneiraSuplementos.store.service.ProductCatalogService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.products.file}")
    private String productsFile;

    private Path productsFilePath;

    @BeforeEach
    void resetProductsFile() throws IOException {
        productsFilePath = Path.of(productsFile).toAbsolutePath().normalize();
        Files.createDirectories(productsFilePath.getParent());
        Files.writeString(
            productsFilePath,
            objectMapper.writeValueAsString(ProductCatalogService.getDefaultCatalog()),
            StandardCharsets.UTF_8
        );
    }

    @Test
    void adicionarProdutoCriaCarrinhoNaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/carrinho/adicionar")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "whey-chocolate"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/carrinho?added"));

        CartSummary summary = cartService.getSummary(session);

        assertThat(summary.getTotalItems()).isEqualTo(1);
        assertThat(summary.getSubtotal()).isEqualByComparingTo("119.99");
        assertThat(summary.getTotal()).isEqualByComparingTo("144.99");
    }

    @Test
    void atualizarQuantidadeRecalculaTotais() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);
        cartService.addProduct("creatina-ouro-em-po", session);

        mockMvc.perform(post("/carrinho/atualizar")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "whey-chocolate")
                .param("quantity", "3"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/carrinho?updated"));

        CartSummary summary = cartService.getSummary(session);

        assertThat(summary.getTotalItems()).isEqualTo(4);
        assertThat(summary.getSubtotal()).isEqualByComparingTo("479.96");
        assertThat(summary.getTotal()).isEqualByComparingTo("504.96");
    }

    @Test
    void removerProdutoEsvaziaCarrinhoQuandoUltimoItemSai() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);

        mockMvc.perform(post("/carrinho/remover")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "whey-chocolate"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/carrinho?removed"));

        CartSummary summary = cartService.getSummary(session);

        assertThat(summary.isEmpty()).isTrue();
        assertThat(summary.getTotalItems()).isZero();
        assertThat(summary.getSubtotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void carrinhoRenderizaItensDaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct("whey-chocolate", session);

        mockMvc.perform(get("/carrinho").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Whey")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Sabor Chocolate")));
    }
}
