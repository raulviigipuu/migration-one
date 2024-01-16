package dev.migrationone.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Person {

    @Id
    Long id

    String name
    Integer age
    String email
}
