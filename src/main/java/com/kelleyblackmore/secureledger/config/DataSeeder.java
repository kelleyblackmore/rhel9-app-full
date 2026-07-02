package com.kelleyblackmore.secureledger.config;

import com.kelleyblackmore.secureledger.entity.AppUser;
import com.kelleyblackmore.secureledger.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the two demo accounts at startup with BCrypt-hashed passwords.
 * Idempotent: only creates users that do not already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seed("admin", "admin123", "ADMIN");
        seed("user", "user123", "USER");
    }

    private void seed(String username, String rawPassword, String role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
            log.info("Seeded user '{}' with role ROLE_{}", username, role);
        }
    }
}
