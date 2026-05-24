package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.cart.CartSummary;
import com.curso.boot.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data.sql")
class CartFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;


    @Test
    void adicionarProdutoCriaCarrinhoNaSessao() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/carrinho/adicionar")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "1"))
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
        cartService.addProduct(1L, session);
        cartService.addProduct(2L, session);

        mockMvc.perform(post("/carrinho/atualizar")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "1")
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
        cartService.addProduct(1L, session);

        mockMvc.perform(post("/carrinho/remover")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .session(session)
                .param("productId", "1"))
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
        cartService.addProduct(1L, session);

        mockMvc.perform(get("/carrinho").session(session))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Whey")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Sabor Chocolate")));
    }
}
