package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.cart.CartSummary;
import com.curso.boot.cart.CartService;
import com.curso.boot.domain.Order;
import com.curso.boot.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
@Transactional
@Sql("/data.sql")
class CheckoutFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Test
    void checkoutExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void finalizarCheckoutSalvaPedidoELimpaCarrinho() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct(1L, session);
        cartService.addProduct(2L, session);

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

        List<Order> orders = orderService.getOrdersByCustomerEmail("teste@betoneira.com");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomerEmail()).isEqualTo("teste@betoneira.com");
        assertThat(orders.get(0).getPaymentMethod()).isEqualTo("PIX");
        assertThat(orders.get(0).getTotal().toString()).isEqualTo("264.98");

        CartSummary summary = cartService.getSummary(session);
        assertThat(summary.isEmpty()).isTrue();
    }

    @Test
    void sucessoRenderizaPedidoSalvo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct(1L, session);

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
