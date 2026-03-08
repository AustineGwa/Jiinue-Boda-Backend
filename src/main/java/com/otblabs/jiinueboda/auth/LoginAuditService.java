package com.otblabs.jiinueboda.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class LoginAuditService {

    private final JdbcTemplate jdbcTemplateOne;

    public LoginAuditService(JdbcTemplate jdbcTemplateOne) {
        this.jdbcTemplateOne = jdbcTemplateOne;
    }

    public void saveAudit(HttpServletRequest request, String userIdentifier,int userId, String status, String failureReason, String sessionId) {

        String userAgent = request.getHeader("User-Agent");

        jdbcTemplateOne.update(
                """
                INSERT INTO login_audits
                    (user_identifier, user_id, status, failure_reason, attempted_at,
                     ip_address, user_agent, device_type, browser, operating_system, session_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userIdentifier,
                userId,
                status,
                failureReason,
                LocalDateTime.now(),
                extractIpAddress(request),
                userAgent,
                parseDeviceType(userAgent),
                parseBrowser(userAgent),
                parseOS(userAgent),
                sessionId
        );
    }

    public void markSuccess(String sessionId, String jwtToken) {
        jdbcTemplateOne.update(
                """
                UPDATE login_audits
                SET status = 'SUCCESS', completed_at = ?, jwt_token_hash = ?
                WHERE session_id = ?
                """,
                LocalDateTime.now(),
                hashToken(jwtToken),
                sessionId
        );
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String parseDeviceType(String ua) {
        if (ua == null) return "Unknown";
        String u = ua.toLowerCase();
        if (u.contains("mobile") || u.contains("android") || u.contains("iphone")) return "Mobile";
        if (u.contains("tablet") || u.contains("ipad")) return "Tablet";
        return "Desktop";
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        String u = ua.toLowerCase();
        if (u.contains("edg/"))    return "Edge";
        if (u.contains("chrome"))  return "Chrome";
        if (u.contains("firefox")) return "Firefox";
        if (u.contains("safari"))  return "Safari";
        if (u.contains("opr"))     return "Opera";
        return "Unknown";
    }

    private String parseOS(String ua) {
        if (ua == null) return "Unknown";
        String u = ua.toLowerCase();
        if (u.contains("windows")) return "Windows";
        if (u.contains("mac os"))  return "macOS";
        if (u.contains("android")) return "Android";
        if (u.contains("iphone") || u.contains("ipad")) return "iOS";
        if (u.contains("linux"))   return "Linux";
        return "Unknown";
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "hash-error";
        }
    }
}
