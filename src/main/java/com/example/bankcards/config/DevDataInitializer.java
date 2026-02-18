package com.example.bankcards.config;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    @Bean
    public ApplicationRunner initTestUsers(UserRepository userRepository,
                                           PasswordEncoder encoder) {
        return args -> {
            // Список тестовых пользователей
            List<User> users = List.of(
                    User.builder()
                            .email("email@email.com")
                            .firstName("TOM")
                            .lastName("SMITH")
                            .password(encoder.encode("12345678"))
                            .role(User.Role.ADMIN)
                            .build(),
                    User.builder()
                            .email("email1@email.com")
                            .firstName("KATE")
                            .lastName("BROWN")
                            .password(encoder.encode("12345678"))
                            .role(User.Role.USER)
                            .build()
            );

            // Сохраняем только если email ещё нет
            users.forEach(user -> {
                if (!userRepository.existsByEmail(user.getEmail())) {
                    userRepository.save(user);
                }
            });
        };
    }
}
