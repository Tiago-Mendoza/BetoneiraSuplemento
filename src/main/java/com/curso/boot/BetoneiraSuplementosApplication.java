package com.curso.boot;

import com.curso.boot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BetoneiraSuplementosApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetoneiraSuplementosApplication.class, args);
    }

    @Bean
    public ApplicationRunner initAdmin(
        UserService userService,
        @Value("${app.admin.email}") String adminEmail,
        @Value("${app.admin.password}") String adminPassword,
        @Value("${app.admin.name}") String adminName
    ) {
        return args -> userService.createAdminIfNotExists(adminEmail, adminPassword, adminName);
    }
}
