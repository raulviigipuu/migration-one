package dev.migrationone

import dev.migrationone.migration.MigrationService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import static org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class MigrationOneApplicationTests {

    @Autowired
    private MigrationService migrationService

    @Test
    void contextLoads() {
        assertNotNull(migrationService, 'MigrationService should be autowired and not null')
    }
}
