package com.kelleyblackmore.secureledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableAsync
public class SecureLedgerApplication {

    public static void main(String[] args) throws Exception {
        ensureDbDirectory();
        SpringApplication.run(SecureLedgerApplication.class, args);
    }

    /**
     * Ensure the SQLite database's parent directory exists before the datasource connects,
     * so the app runs both in-container (/app/data) and locally without manual setup.
     */
    private static void ensureDbDirectory() throws Exception {
        String dbPath = System.getenv().getOrDefault("DB_PATH", "/app/data/secureledger.db");
        Path parent = Paths.get(dbPath).toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
