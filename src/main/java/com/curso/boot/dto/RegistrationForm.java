package com.curso.boot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Informe seu nome completo.")
    @Size(min = 3, max = 100, message = "Informe um nome com entre 3 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu CPF.")
    @Size(max = 20, message = "CPF inválido.")
    private String cpf;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "Informe seu telefone celular.")
    @Size(max = 20, message = "Telefone inválido.")
    private String telefoneCelular;

    @Size(max = 20, message = "Telefone inválido.")
    private String telefoneFixo;

    @NotBlank(message = "Selecione um gênero.")
    private String genero;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 6, max = 15, message = "A senha deve conter entre 6 e 15 caracteres.")
    private String password;

    @NotBlank(message = "Confirme sua senha.")
    @Size(max = 15, message = "A senha deve conter no máximo 15 caracteres.")
    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefoneCelular() {
        return telefoneCelular;
    }

    public void setTelefoneCelular(String telefoneCelular) {
        this.telefoneCelular = telefoneCelular;
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String telefoneFixo) {
        this.telefoneFixo = telefoneFixo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
