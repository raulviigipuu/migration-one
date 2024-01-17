package dev.migrationone.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

import java.time.LocalDateTime

@Entity
class MigrationHistory {

    @Id
    Long id

    String migrationFile
    LocalDateTime executedAt
}
