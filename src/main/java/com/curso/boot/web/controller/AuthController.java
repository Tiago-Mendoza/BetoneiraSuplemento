package com.curso.boot.web.controller;

import com.curso.boot.dto.RegistrationForm;
import com.curso.boot.exception.DuplicateUserFieldException;
import com.curso.boot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService UserService;

    public AuthController(UserService UserService) {
        this.UserService = UserService;
    }

    @ModelAttribute("cadastroForm")
    public RegistrationForm cadastroForm() {
        return new RegistrationForm();
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/home";
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
        @Valid @ModelAttribute("cadastroForm") RegistrationForm cadastroForm,
        BindingResult bindingResult
    ) {
        validateRegistrationData(cadastroForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "cadastro";
        }

        try {
            UserService.register(cadastroForm);
        } catch (DuplicateUserFieldException exception) {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
            return "cadastro";
        }

        return "redirect:/login?registered";
    }

    private void validateRegistrationData(RegistrationForm cadastroForm, BindingResult bindingResult) {
        String cpf = digitsOnly(cadastroForm.getCpf());
        if (!cpf.isBlank() && cpf.length() != 11) {
            bindingResult.rejectValue("cpf", "invalid", "Informe um CPF com 11 dígitos.");
        }

        String telefoneCelular = digitsOnly(cadastroForm.getTelefoneCelular());
        if (!telefoneCelular.isBlank() && (telefoneCelular.length() < 10 || telefoneCelular.length() > 11)) {
            bindingResult.rejectValue(
                "telefoneCelular",
                "invalid",
                "Informe um telefone celular com 10 ou 11 dígitos."
            );
        }

        String telefoneFixo = digitsOnly(cadastroForm.getTelefoneFixo());
        if (!telefoneFixo.isBlank() && (telefoneFixo.length() < 10 || telefoneFixo.length() > 11)) {
            bindingResult.rejectValue(
                "telefoneFixo",
                "invalid",
                "Informe um telefone fixo com 10 ou 11 dígitos."
            );
        }

        // Validar confirmação de senha
        if (cadastroForm.getPassword() != null && cadastroForm.getConfirmPassword() != null) {
            if (!cadastroForm.getPassword().equals(cadastroForm.getConfirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "mismatch", "As senhas não coincidem.");
            }
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
