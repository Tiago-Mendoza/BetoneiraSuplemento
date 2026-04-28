package com.curso.boot.BetoneiraSuplementos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.users.file}")
    private String usersFile;

    private Path usersFilePath;

    @BeforeEach
    void resetUsersFile() throws IOException {
        usersFilePath = Path.of(usersFile).toAbsolutePath().normalize();
        Files.createDirectories(usersFilePath.getParent());
        Files.writeString(usersFilePath, "[]", StandardCharsets.UTF_8);
    }

    @Test
    void cadastroValidoSalvaUsuarioNoJson() throws Exception {
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

        List<Map<String, Object>> users = objectMapper.readValue(
            Files.readString(usersFilePath, StandardCharsets.UTF_8),
            new TypeReference<>() {}
        );

        assertThat(users).hasSize(1);
        assertThat(users.get(0).get("email")).isEqualTo("teste@betoneira.com");
        assertThat(users.get(0).get("cpf")).isEqualTo("12345678901");
        assertThat((String) users.get(0).get("passwordHash")).isNotEqualTo("123456");
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
