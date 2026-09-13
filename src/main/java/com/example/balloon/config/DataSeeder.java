package com.example.balloon.config;

import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.repository.UserRepository;
import com.example.balloon.service.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Создаёт учётную запись администратора при первом запуске.
 * Как и в Go-версии, проверяется наличие любого пользователя с ролью admin.
 */
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public DataSeeder(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUserRole("admin") || userRepository.existsByUserName("admin")) {
            return;
        }

        byte[] salt = passwordService.generateSalt();

        UserEntity admin = new UserEntity();
        admin.setUserName("admin");
        admin.setUserEmail("admin@localhost");
        admin.setUserRole("admin");
        admin.setPasswordHash(passwordService.hashPassword("admin123", salt));
        admin.setSalt(passwordService.encodeSalt(salt));

        userRepository.save(admin);
        log.info("Создан администратор по умолчанию: admin / admin123");
    }
}
