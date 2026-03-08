package com.otblabs.jiinueboda.auth;

import com.auth0.jwt.JWT;
import com.otblabs.jiinueboda.exceptions.ExceptionsHandlerService;
import com.otblabs.jiinueboda.security.SecurityConstants;
import com.otblabs.jiinueboda.users.UserService;
import com.otblabs.jiinueboda.users.models.SystemUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TwoFactorService twoFactorService;
    private final LoginAuditService loginAuditService;
    private final ExceptionsHandlerService exceptionsHandlerService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse<Object>> adminLogin(
            @RequestBody Credentials credentials,
            HttpServletRequest request) {

        String sessionId = UUID.randomUUID().toString();

        try {
            SystemUser systemUser = userService.getByEmailOrPhone(credentials.getUser());

            if (systemUser == null) {
                loginAuditService.saveAudit(request, credentials.getUser(),
                        0, "USER_NOT_FOUND", "User not found", sessionId);
                return ResponseEntity.status(404)
                        .body(new LoginResponse<>("Error: user does not exist", null));
            }

            if (!bCryptPasswordEncoder.matches(credentials.getPassword(), systemUser.getPassword())) {
                loginAuditService.saveAudit(request, credentials.getUser(),
                        systemUser.getId(), "WRONG_PASSWORD", "Wrong password", sessionId);
                return ResponseEntity.status(401)
                        .body(new LoginResponse<>("Error: wrong email or password combination", null));
            }

            // Password OK — fire OTP
            twoFactorService.generateAndSendOtp(credentials.getUser(), systemUser.getPhone());

            loginAuditService.saveAudit(request, credentials.getUser(),
                    systemUser.getId(), "OTP_SENT", null, sessionId);

            return ResponseEntity.ok(
                    new LoginResponse<>("OTP sent to your registered phone number", null, true, sessionId)
            );

        } catch (Exception e) {
            e.printStackTrace();
            exceptionsHandlerService.saveExceptionToDb(e.getMessage(), 3);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse<Object>> verifyOtp(
            @RequestBody OtpVerificationRequest otpRequest,
            HttpServletRequest request) {

        try {
            SystemUser systemUser = userService.getByEmailOrPhone(otpRequest.getUserIdentifier());

            if (systemUser == null) {
                return ResponseEntity.status(404)
                        .body(new LoginResponse<>("User not found", null));
            }

            TwoFactorService.OtpResult result =
                    twoFactorService.verifyOtp(otpRequest.getUserIdentifier(), otpRequest.getOtpCode());

            switch (result) {
                case EXPIRED -> {
                    loginAuditService.saveAudit(request, otpRequest.getUserIdentifier(),
                            systemUser.getId(), "OTP_EXPIRED", "OTP expired", otpRequest.getSessionId());
                    return ResponseEntity.status(401)
                            .body(new LoginResponse<>("OTP has expired, please login again", null));
                }
                case INVALID, NOT_FOUND -> {
                    loginAuditService.saveAudit(request, otpRequest.getUserIdentifier(),
                            systemUser.getId(), "OTP_FAILED", "Invalid OTP", otpRequest.getSessionId());
                    return ResponseEntity.status(401)
                            .body(new LoginResponse<>("Invalid OTP code", null));
                }
                case SUCCESS -> {
                    String token = JWT.create()
                            .withSubject(otpRequest.getUserIdentifier())
                            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                            .sign(HMAC512(SecurityConstants.SECRET.getBytes()));

                    loginAuditService.markSuccess(otpRequest.getSessionId(), token);

                    return ResponseEntity.ok(new LoginResponse<>(token, systemUser));
                }
                default -> { return ResponseEntity.internalServerError().build(); }
            }

        } catch (Exception e) {
            e.printStackTrace();
            exceptionsHandlerService.saveExceptionToDb(e.getMessage(), 3);
            return ResponseEntity.internalServerError().build();
        }
    }
}