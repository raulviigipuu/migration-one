package dev.migrationone.migration

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Timestamp

@Slf4j
@Service
class MigrationService implements ApplicationListener<ApplicationReadyEvent> {
    public static final Path MIGRATIONS_PATH = Paths.get("src/main/resources/migrations")
    public static final String IGNORE_SUFFIX = "_ignore.sql"

    private final Sql sql

    MigrationService(DataSource dataSource) {
        this.sql = new Sql(dataSource)
    }

    @PostConstruct
    void init() {
        log.info("🚀 Initializing migration service")
        // Create a table for tracking migrations if it doesn't exist
        sql.execute("""
            CREATE TABLE IF NOT EXISTS migration_history (
                id INT AUTO_INCREMENT PRIMARY KEY,
                migration_file VARCHAR(255),
                executed_at TIMESTAMP
            )
        """)
    }

    @Override
    void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("🚀 Application ready. Checking and running migrations.")
        checkAndRunMigrations()
    }

    private void checkAndRunMigrations() {
        Path migrationsPath = MIGRATIONS_PATH

        try {
            Files.walk(migrationsPath)
                    .findAll { Path it -> it.toFile().isFile() && it.toFile().name.endsWith('.sql') && !it.toFile().name.endsWith(IGNORE_SUFFIX) }
                    .sort { Path it -> it.toFile().name }
                    .each { Path it -> applyMigration(it) }
        } catch (IOException e) {
            log.error("⚠️ Error reading migration files", e)
            throw new RuntimeException("⚠️ Error reading migration files", e)
        }
    }

    @Transactional
    void applyMigration(Path migration) {
        try {
            String migrationFileName = migration.getFileName().toString()
            log.debug("🔍 Checking migration file: $migrationFileName")

            // Check if this migration has been executed
            def result = sql.firstRow("SELECT COUNT(*) FROM migration_history WHERE migration_file = ?", [migrationFileName])?.get(0)
            Integer count = result ? result.get(0) : 0

            if (count == 0) {
                log.info("🚀 Executing migration: $migrationFileName")
                String sqlContent = Files.readString(migration, StandardCharsets.UTF_8)
                sql.execute(sqlContent)

                // Record the migration execution
                sql.execute("""
                    INSERT INTO migration_history (migration_file, executed_at)
                    VALUES (?, ?)
                """, [migrationFileName, new Timestamp(System.currentTimeMillis())])

                log.info("✅ Executed migration: $migrationFileName")
            } else {
                log.info("✅✅ Migration already applied: $migrationFileName")
            }
        } catch (Exception e) {
            log.error("️⚠️ Migration failed: " + migration.getFileName(), e)
            throw new RuntimeException("⚠️ Migration failed: " + migration.getFileName(), e)
        }
    }
}
