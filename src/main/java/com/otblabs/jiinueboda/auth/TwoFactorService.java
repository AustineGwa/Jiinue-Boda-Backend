package com.otblabs.jiinueboda.auth;

import com.otblabs.jiinueboda.sms.SmsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TwoFactorService {

    private final JdbcTemplate jdbcTemplateOne;
    private final SmsService smsService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    public TwoFactorService(JdbcTemplate jdbcTemplateOne, SmsService smsService) {
        this.jdbcTemplateOne = jdbcTemplateOne;
        this.smsService = smsService;
    }

    public void generateAndSendOtp(String userIdentifier, String phoneNumber) throws Exception {
        // Invalidate existing unused OTPs
        jdbcTemplateOne.update(
                "UPDATE otp_tokens SET used = TRUE WHERE user_identifier = ? AND used = FALSE",
                userIdentifier
        );

        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplateOne.update(
                "INSERT INTO otp_tokens (user_identifier, otp_code, expires_at, used, created_at) VALUES (?, ?, ?, FALSE, ?)",
                userIdentifier,
                otp,
                now.plusMinutes(OTP_EXPIRY_MINUTES),
                now
        );

        String message = "Your verification code is: " + otp +
                ". Valid for " + OTP_EXPIRY_MINUTES + " minutes. Do not share this code.";
        smsService.sendLoginOTPSms(phoneNumber, message);
    }

    public OtpResult verifyOtp(String userIdentifier, String submittedOtp) {
        List<Map<String, Object>> rows = jdbcTemplateOne.queryForList(
                "SELECT * FROM otp_tokens WHERE user_identifier = ? AND used = FALSE ORDER BY created_at DESC LIMIT 1",
                userIdentifier
        );

        if (rows.isEmpty()) {
            return OtpResult.NOT_FOUND;
        }

        Map<String, Object> token = rows.get(0);
        LocalDateTime expiresAt = (LocalDateTime) token.get("expires_at");

        if (LocalDateTime.now().isAfter(expiresAt)) {
            jdbcTemplateOne.update(
                    "UPDATE otp_tokens SET used = TRUE WHERE id = ?",
                    token.get("id")
            );
            return OtpResult.EXPIRED;
        }

        if (!submittedOtp.equals(token.get("otp_code"))) {
            return OtpResult.INVALID;
        }

        // Valid — mark used
        jdbcTemplateOne.update(
                "UPDATE otp_tokens SET used = TRUE, verified_at = ? WHERE id = ?",
                LocalDateTime.now(),
                token.get("id")
        );

        return OtpResult.SUCCESS;
    }

    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

    public enum OtpResult {
        SUCCESS, INVALID, EXPIRED, NOT_FOUND
    }
}
