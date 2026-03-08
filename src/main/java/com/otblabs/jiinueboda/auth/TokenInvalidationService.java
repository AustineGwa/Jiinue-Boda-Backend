package com.otblabs.jiinueboda.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenInvalidationService {

    private final JdbcTemplate jdbcTemplateOne;

    // Invalidate by raw token (e.g. from logout endpoint)
    public void invalidateToken(String rawToken, String invalidatedBy, String reason, Date tokenExpiry) {
        String hash = hashToken(rawToken);
        jdbcTemplateOne.update(
                """
                INSERT INTO invalidated_tokens (token_hash, invalidated_at, invalidated_by, reason, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                hash,
                LocalDateTime.now(),
                invalidatedBy,
                reason,
                tokenExpiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    // Invalidate by token hash directly (e.g. from login_audits lookup)
    public void invalidateByHash(String tokenHash, String invalidatedBy, String reason, LocalDateTime expiresAt) {
        jdbcTemplateOne.update(
                """
                INSERT INTO invalidated_tokens (token_hash, invalidated_at, invalidated_by, reason, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                tokenHash,
                LocalDateTime.now(),
                invalidatedBy,
                reason,
                expiresAt
        );
    }

    public boolean isTokenInvalidated(String rawToken) {
        String hash = hashToken(rawToken);
        Integer count = jdbcTemplateOne.queryForObject(
                "SELECT COUNT(*) FROM invalidated_tokens WHERE token_hash = ?",
                Integer.class,
                hash
        );
        return count != null && count > 0;
    }

    // Called on a schedule to keep the table lean
    public void cleanupExpiredTokens() {
        jdbcTemplateOne.update(
                "DELETE FROM invalidated_tokens WHERE expires_at < ?",
                LocalDateTime.now()
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

}
