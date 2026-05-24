package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.cart.CartService;
import com.curso.boot.domain.Order;
import com.curso.boot.domain.Product;
import com.curso.boot.service.OrderService;
import com.curso.boot.service.ProductService;
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
@Transactional
@Sql("/data.sql")
class OrderHistoryAndAdminIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

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
                .param("productId", "1")
                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos?created"));

        List<Order> createdOrders = orderService.getOrdersByCustomerEmail("manual@betoneira.com");
        assertThat(createdOrders).hasSize(1);
        assertThat(createdOrders.get(0).getSource()).isEqualTo("Admin");
        assertThat(createdOrders.get(0).getCustomerEmail()).isEqualTo("manual@betoneira.com");

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

        List<Order> updatedOrders = orderService.getOrdersByCustomerEmail("atualizado@betoneira.com");
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

        assertThat(orderService.getOrdersByCustomerEmail("atualizado@betoneira.com")).isEmpty();
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

        List<Product> createdProducts = productService.getAllProducts();
        Product createdProduct = createdProducts.stream()
                .filter(product -> product.getName().equals("Pre Treino")
                        && product.getSubtitle().equals("Sabor Laranja"))
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

        List<Product> updatedProducts = productService.getAllProducts();
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

        assertThat(productService.getAllProducts()).extracting(Product::getId).doesNotContain(createdProduct.getId());
    }

    private String criarPedidoViaCheckout(String email, String recipientName) throws Exception {
        MockHttpSession session = new MockHttpSession();
        cartService.addProduct(1L, session);

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

}
