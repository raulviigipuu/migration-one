package dev.migrationone.migration

import groovy.sql.Sql
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import javax.sql.DataSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@SpringBootTest
@ActiveProfiles('test')
class MigrationServiceIntegrationTest {

    @Autowired
    DataSource dataSource

    @Test
    void testMigrationHistoryTable() {
        Sql sql = new Sql(dataSource)

        // Check if the "migration_history" table exists
        boolean tableExists = sql.firstRow("""
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = 'PUBLIC' 
            AND TABLE_NAME = 'MIGRATION_HISTORY'
        """)[0] > 0
        assertTrue(tableExists, 'Migration history table should exist')

        // Check if there are 2 entries in the "migration_history" table
        int count = sql.firstRow("""
            SELECT COUNT(*) 
            FROM migration_history
        """)[0] as int
        assertEquals(2, count, 'There should be 2 entries in the migration history table')

        List<String> expectedOrder = ["2024-01-15_initial _schema.sql", "2024-01-16_add_column.sql"]
        // Query all rows in order of execution
        List<String> actualOrder = sql.rows("""
            SELECT migration_file 
            FROM migration_history 
            ORDER BY executed_at, id
        """).collect { it.migration_file as String }

        assertEquals(expectedOrder, actualOrder, 'Migrations should be executed in the correct order')
    }

    @Test
    void testPersonTable() {

        Sql sql = new Sql(dataSource)
        def tableName = 'person'

        // Check if the "person" table exists
        boolean tableExists = sql.firstRow("""
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = 'PUBLIC' 
            AND TABLE_NAME = '${tableName.toUpperCase()}'
        """)[0] > 0
        assertTrue(tableExists, 'Person table should exist')

        def expectedColumns = ["id", "name", "age", "email"]

        expectedColumns.each { column ->
            int count = sql.firstRow("""
                SELECT COUNT(*) 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = '${tableName.toUpperCase()}' 
                AND COLUMN_NAME = '${column.toUpperCase()}'
            """)[0] as int

            assertTrue(count > 0, "Column $column does not exist in $tableName")
        }
    }
}
