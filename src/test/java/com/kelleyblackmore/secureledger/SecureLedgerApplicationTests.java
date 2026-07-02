package com.kelleyblackmore.secureledger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SecureLedgerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring context (security, JPA, filters, controllers) wires up.
    }
}
