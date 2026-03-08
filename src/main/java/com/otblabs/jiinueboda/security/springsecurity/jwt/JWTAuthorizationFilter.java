package com.otblabs.jiinueboda.security.springsecurity.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.otblabs.jiinueboda.auth.TokenInvalidationService;
import com.otblabs.jiinueboda.security.SecurityConstants;
import com.otblabs.jiinueboda.utility.generic.exception.ErrorResponse;
import com.otblabs.jiinueboda.utility.generic.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private UserDetailsService userDetailsService;
    private TokenInvalidationService tokenInvalidationService;

    public JWTAuthorizationFilter(UserDetailsService userDetailsService,
                                  AuthenticationManager authManager,
                                  TokenInvalidationService tokenInvalidationService) {
        super(authManager);
        this.userDetailsService = userDetailsService;
        this.tokenInvalidationService = tokenInvalidationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(SecurityConstants.HEADER_STRING);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        // check blacklist before anything else
        String rawToken = header.replace(SecurityConstants.TOKEN_PREFIX, "").trim();
        if (tokenInvalidationService.isTokenInvalidated(rawToken)) {
            writeErrorResponse(res, new ErrorResponse(401, "Session has been invalidated. Please login again."));
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;

        try {
            authentication = getAuthentication(req, res);
        } catch (UnauthorizedException e) {
            writeErrorResponse(res, new ErrorResponse(403, e.getMessage()));
        } catch (SignatureVerificationException e) {
            writeErrorResponse(res, new ErrorResponse(403, "Invalid token signature"));
        } catch (TokenExpiredException e) {
            writeErrorResponse(res, new ErrorResponse(403, e.getMessage()));
        } catch (AccessDeniedException e) {
            writeErrorResponse(res, new ErrorResponse(403, "Invalid access token"));
        } catch (JWTDecodeException e) {
            writeErrorResponse(res, new ErrorResponse(403, "Invalid access token"));
        }

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(req, res);
        } else
            return;
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, HttpServletResponse res) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        String user = null;

        String token = bearerToken.replace(SecurityConstants.TOKEN_PREFIX, "").trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException();
        } else
            user = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET.getBytes())).build()
                    .verify(token).getSubject();

        UserDetails details = this.userDetailsService.loadUserByUsername(user);
        return new UsernamePasswordAuthenticationToken(details, "", details.getAuthorities());
    }

    private void writeErrorResponse(HttpServletResponse res, ErrorResponse response) {
        try {
            res.setStatus(response.getCode());
            PrintWriter out = res.getWriter();
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            out.print(new ObjectMapper().writeValueAsString(response));
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
