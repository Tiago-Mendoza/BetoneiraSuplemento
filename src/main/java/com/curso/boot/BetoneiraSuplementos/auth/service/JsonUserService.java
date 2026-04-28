package com.curso.boot.BetoneiraSuplementos.auth.service;

import com.curso.boot.BetoneiraSuplementos.auth.dto.RegistrationForm;
import com.curso.boot.BetoneiraSuplementos.auth.exception.DuplicateUserFieldException;
import com.curso.boot.BetoneiraSuplementos.auth.model.StoredUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class JsonUserService implements UserDetailsService {

    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final Path usersFilePath;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;
    private final TypeReference<List<StoredUser>> usersType = new TypeReference<>() {};
    private final Object fileLock = new Object();

    public JsonUserService(
        ObjectMapper objectMapper,
        PasswordEncoder passwordEncoder,
        @Value("${app.users.file:./data/users.json}") String usersFilePath,
        @Value("${app.admin.email:admin@betoneira.com}") String adminEmail,
        @Value("${app.admin.password:123456}") String adminPassword,
        @Value("${app.admin.name:Administrador}") String adminName
    ) {
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.usersFilePath = Path.of(usersFilePath).toAbsolutePath().normalize();
        this.adminEmail = normalizeEmail(adminEmail);
        this.adminPassword = adminPassword;
        this.adminName = adminName == null ? "Administrador" : adminName.trim();
    }

    @PostConstruct
    void initializeStore() {
        synchronized (fileLock) {
            try {
                Path parent = usersFilePath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                if (Files.notExists(usersFilePath) || Files.size(usersFilePath) == 0) {
                    writeUsers(new ArrayList<>());
                }
                ensureAdminAccount();
            } catch (IOException exception) {
                throw new IllegalStateException("Não foi possível inicializar o arquivo de usuários.", exception);
            }
        }
    }

    public void register(RegistrationForm form) {
        synchronized (fileLock) {
            List<StoredUser> users = readUsers();
            String normalizedEmail = normalizeEmail(form.getEmail());
            String normalizedCpf = digitsOnly(form.getCpf());

            if (users.stream().anyMatch(user -> normalizedEmail.equals(user.getEmail()))) {
                throw new DuplicateUserFieldException("email", "Este e-mail já está cadastrado.");
            }

            if (users.stream().anyMatch(user -> normalizedCpf.equals(user.getCpf()))) {
                throw new DuplicateUserFieldException("cpf", "Este CPF já está cadastrado.");
            }

            users.add(buildUser(form, normalizedEmail, normalizedCpf));
            writeUsers(users);
        }
    }

    private void ensureAdminAccount() {
        List<StoredUser> users = readUsers();
        Optional<StoredUser> existingAdmin = users.stream()
            .filter(user -> adminEmail.equals(user.getEmail()))
            .findFirst();

        if (existingAdmin.isPresent()) {
            StoredUser adminUser = existingAdmin.get();
            if (!adminUser.getRoles().contains("ADMIN")) {
                List<String> roles = new ArrayList<>(adminUser.getRoles());
                roles.add("ADMIN");
                adminUser.setRoles(roles);
                writeUsers(users);
            }
            return;
        }

        StoredUser adminUser = new StoredUser();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setEmail(adminEmail);
        adminUser.setNome(adminName);
        adminUser.setPasswordHash(passwordEncoder.encode(adminPassword));
        adminUser.setRoles(List.of("ADMIN", "USER"));
        users.add(adminUser);
        writeUsers(users);
    }

    public Optional<StoredUser> findByEmail(String email) {
        synchronized (fileLock) {
            String normalizedEmail = normalizeEmail(email);
            return readUsers().stream()
                .filter(storedUser -> normalizedEmail.equals(storedUser.getEmail()))
                .findFirst();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        synchronized (fileLock) {
            String normalizedEmail = normalizeEmail(email);

            StoredUser user = readUsers().stream()
                .filter(storedUser -> normalizedEmail.equals(storedUser.getEmail()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

            List<String> roles = user.getRoles().isEmpty() ? List.of("USER") : user.getRoles();

            return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(roles.toArray(String[]::new))
                .build();
        }
    }

    private StoredUser buildUser(RegistrationForm form, String normalizedEmail, String normalizedCpf) {
        StoredUser user = new StoredUser();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(normalizedEmail);
        user.setCpf(normalizedCpf);
        user.setNome(normalizeName(form.getNome()));
        user.setDataNascimento(form.getDataNascimento());
        user.setTelefoneCelular(digitsOnly(form.getTelefoneCelular()));
        user.setTelefoneFixo(normalizeOptionalDigits(form.getTelefoneFixo()));
        user.setGenero(form.getGenero());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRoles(List.of("USER"));
        return user;
    }

    private List<StoredUser> readUsers() {
        try {
            if (Files.notExists(usersFilePath) || Files.size(usersFilePath) == 0) {
                return new ArrayList<>();
            }

            try (InputStream inputStream = Files.newInputStream(usersFilePath)) {
                List<StoredUser> users = objectMapper.readValue(inputStream, usersType);
                return users == null ? new ArrayList<>() : new ArrayList<>(users);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o arquivo de usuários.", exception);
        }
    }

    private void writeUsers(List<StoredUser> users) {
        try (OutputStream outputStream = Files.newOutputStream(
            usersFilePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, users);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar o arquivo de usuários.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String nome) {
        return nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
    }

    private String normalizeOptionalDigits(String value) {
        String digits = digitsOnly(value);
        return StringUtils.hasText(digits) ? digits : null;
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
