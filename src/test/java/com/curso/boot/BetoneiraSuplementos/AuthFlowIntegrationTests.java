package com.curso.boot.BetoneiraSuplementos;

import com.curso.boot.domain.User;
import com.curso.boot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void cadastroValidoSalvaUsuarioNoBanco() throws Exception {
        mockMvc.perform(post("/cadastro")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("email", "teste@betoneira.com")
                .param("cpf", "123.456.789-01")
                .param("nome", "Usuário Teste")
                .param("dataNascimento", "2000-01-01")
                .param("telefoneCelular", "11999999999")
                .param("telefoneFixo", "1133334444")
                .param("genero", "Masculino")
                .param("password", "123456"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registered"));

        User user = userService.findByEmail("teste@betoneira.com");

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("teste@betoneira.com");
        assertThat(user.getCpf()).isEqualTo("12345678901");
        assertThat(user.getPasswordHash()).isNotEqualTo("123456");
    }

    @Test
    void cadastroNaoPermiteEmailOuCpfDuplicado() throws Exception {
        registrarUsuarioPadrao();

        mockMvc.perform(post("/cadastro")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("email", "teste@betoneira.com")
                .param("cpf", "98765432100")
                .param("nome", "Outro Usuário")
                .param("dataNascimento", "2001-02-02")
                .param("telefoneCelular", "11911111111")
                .param("telefoneFixo", "")
                .param("genero", "Feminino")
                .param("password", "654321"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("cadastroForm", "email"));

        mockMvc.perform(post("/cadastro")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("email", "novo@betoneira.com")
                .param("cpf", "12345678901")
                .param("nome", "Usuário CPF Duplicado")
                .param("dataNascimento", "2001-02-02")
                .param("telefoneCelular", "11911111111")
                .param("telefoneFixo", "")
                .param("genero", "Feminino")
                .param("password", "654321"))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("cadastroForm", "cpf"));
    }

    @Test
    void loginAutenticaComEmailESenha() throws Exception {
        registrarUsuarioPadrao();

        mockMvc.perform(post("/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("email", "teste@betoneira.com")
                .param("password", "123456"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"))
            .andExpect(authenticated().withUsername("teste@betoneira.com"));
    }

    private void registrarUsuarioPadrao() throws Exception {
        mockMvc.perform(post("/cadastro")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("email", "teste@betoneira.com")
                .param("cpf", "12345678901")
                .param("nome", "Usuário Teste")
                .param("dataNascimento", "2000-01-01")
                .param("telefoneCelular", "11999999999")
                .param("telefoneFixo", "1133334444")
                .param("genero", "Masculino")
                .param("password", "123456"))
            .andExpect(status().is3xxRedirection());
    }
}
