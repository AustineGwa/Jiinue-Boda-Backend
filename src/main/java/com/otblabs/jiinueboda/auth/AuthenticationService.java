package com.otblabs.jiinueboda.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.otblabs.jiinueboda.security.SecurityConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class AuthenticationService {

    private final JdbcTemplate jdbcTemplateOne;
    private final TokenInvalidationService tokenInvalidationService;

    public AuthenticationService(JdbcTemplate jdbcTemplateOne, TokenInvalidationService tokenInvalidationService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.tokenInvalidationService = tokenInvalidationService;
    }

    public String logoutUser(String token) throws Exception{
        DecodedJWT decoded = JWT.require(HMAC512(SecurityConstants.SECRET.getBytes()))
                .build()
                .verify(token);

        tokenInvalidationService.invalidateToken(
                token,
                decoded.getSubject(),
                "User logout",
                decoded.getExpiresAt()
        );

        return "Logged out successfully";
    }

    public String invalidateAllUserSessions(int userId, String reason) throws Exception{
        // Fetch all active token hashes for this user from login_audits
        List<Map<String, Object>> activeSessions = jdbcTemplateOne.queryForList(
                """
                SELECT jwt_token_hash, attempted_at FROM login_audits
                WHERE user_id = ? AND status = 'SUCCESS' AND jwt_token_hash IS NOT NULL
                AND attempted_at > ?
                """,
                userId,
                LocalDateTime.now().minusSeconds(SecurityConstants.EXPIRATION_TIME / 1000)
        );

        activeSessions.forEach(session -> {
            tokenInvalidationService.invalidateByHash(
                    (String) session.get("jwt_token_hash"),
                    "admin",
                    reason,
                    LocalDateTime.now().plusSeconds(SecurityConstants.EXPIRATION_TIME / 1000)
            );
        });

        return "Invalidated " + activeSessions.size() + " session(s)";
    }
}
