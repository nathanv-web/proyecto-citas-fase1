package com.proyectocitas.repository;

import com.proyectocitas.model.EmailVerificationToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Query("SELECT t.usuario.idUsuario FROM EmailVerificationToken t WHERE t.tokenHash = :hash")
    Optional<Long> obtenerUsuarioPorHash(@Param("hash") String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.tokenHash = :hash")
    Optional<EmailVerificationToken> buscarParaUso(@Param("hash") String hash);
}
