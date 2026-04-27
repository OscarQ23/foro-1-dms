package com.oscar.notasapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una cuenta de usuario almacenada localmente.
 *
 * NOTA DE SEGURIDAD: Para una aplicación real la contraseña debe almacenarse
 * con un hash (bcrypt, scrypt, Argon2). En este proyecto académico se guarda
 * en texto plano por simplicidad y para evitar añadir dependencias adicionales.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val password: String
)
