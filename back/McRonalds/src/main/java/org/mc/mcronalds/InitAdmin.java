package org.mc.mcronalds;

import org.mc.mcronalds.model.Role;
import org.mc.mcronalds.model.User;
import org.mc.mcronalds.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class InitAdmin {

    @Bean
    CommandLineRunner init(UserRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.findByUsername("admin").isEmpty()) {

                User admin = User.builder()
                        .username("admin")
                        .password(encoder.encode("123"))  // contraseña real
                        .fullName("Administrador General")
                        .email("admin@mcronalds.com")
                        .role(Role.ADMIN)
                        .createAt(LocalDateTime.now())
                        .build();

                repo.save(admin);

                System.out.println("=== ADMIN CREADO ===");
            }
        };
    }
}