package com.proyectocitas.repository;

import com.proyectocitas.model.PasswordResetToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @Query("SELECT t.usuario.idUsuario FROM PasswordResetToken t WHERE t.tokenHash = :hash")
    Optional<Long> obtenerUsuarioPorHash(@Param("hash") String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM PasswordResetToken t WHERE t.tokenHash = :hash")
    Optional<PasswordResetToken> buscarParaUso(@Param("hash") String hash);
}
