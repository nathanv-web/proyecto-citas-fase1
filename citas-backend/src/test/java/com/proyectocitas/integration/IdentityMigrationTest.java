package com.proyectocitas.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityMigrationTest {
    @Test
    void scriptsAgreganTablasCamposRestriccionesYConservanUsuariosExistentes() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:identity-migrations;MODE=MariaDB", "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE usuarios (id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY)");
            statement.execute("INSERT INTO usuarios (id_usuario) VALUES (1)");
            try (var reset = Files.newBufferedReader(Path.of("database/migrations/2026_10_28_recuperacion_password.sql"));
                 var verify = Files.newBufferedReader(Path.of("database/migrations/2026_10_28_verificacion_correo.sql"))) {
                RunScript.execute(connection, reset);
                RunScript.execute(connection, verify);
            }
            try (var columns = connection.getMetaData().getColumns(null, null, "PASSWORD_RESET_TOKENS", "TOKEN_HASH")) {
                assertTrue(columns.next());
                assertEquals(64, columns.getInt("COLUMN_SIZE"));
                assertEquals(java.sql.DatabaseMetaData.columnNoNulls, columns.getInt("NULLABLE"));
            }
            try (var users = statement.executeQuery("SELECT correo_verificado, version_registro FROM usuarios WHERE id_usuario = 1")) {
                assertTrue(users.next());
                assertFalse(users.getBoolean(1));
                assertEquals(0, users.getLong(2));
            }
            assertThrows(java.sql.SQLException.class, () -> statement.execute("""
                INSERT INTO password_reset_tokens (id_usuario, token_hash, correo, version_sesion, creado_en, expira_en)
                VALUES (99, REPEAT('a', 64), 'prueba@example.com', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """));
            try (var verification = statement.executeQuery("SELECT COUNT(*) FROM email_verification_tokens")) {
                assertTrue(verification.next());
                assertEquals(0, verification.getInt(1));
            }
        }
    }
}
