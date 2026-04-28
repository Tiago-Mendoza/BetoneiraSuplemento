package com.curso.boot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class RegistrationForm {

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail vÃ¡lido.")
    private String email;

    @NotBlank(message = "Informe seu CPF.")
    private String cpf;

    @NotBlank(message = "Informe seu nome completo.")
    @Size(min = 3, max = 100, message = "Informe um nome com pelo menos 3 caracteres.")
    private String nome;

    @NotNull(message = "Informe sua data de nascimento.")
    @Past(message = "Informe uma data de nascimento vÃ¡lida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotBlank(message = "Informe seu telefone celular.")
    private String telefoneCelular;

    private String telefoneFixo;

    @NotBlank(message = "Selecione um gÃªnero.")
    private String genero;

    @NotBlank(message = "Crie uma senha.")
    @Pattern(regexp = "\\d{6}", message = "A senha deve conter 6 dÃ­gitos.")
    private String password;

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

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
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
}
