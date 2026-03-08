package com.otblabs.jiinueboda.security.springsecurity;

import com.otblabs.jiinueboda.security.springsecurity.jwt.JWTAuthenticationFilter;
import com.otblabs.jiinueboda.security.springsecurity.jwt.JWTAuthorizationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
          throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,UserDetailsService userDetailsService,AuthenticationManager authenticationManager) throws Exception {

    http
            .csrf(csrf -> csrf.disable())
            .cors().configurationSource(corsConfigurationSource()).and()
            .authorizeHttpRequests(auth -> auth.requestMatchers(
                            "/auth/login",
                            "/auth/login/*",
                            "/users/invite/createuser",
                            "/location-services/options",
                            "/groups/wards/*",
                            "/payments/momo/poststkpush/*",
                            "/test/banking/im",
                            "/prod/banking/im",
                            "/payments/momo/postb2c/*",
                            "/payments/momo/postc2b/*",
                            "/payments/momo/postbuygoods/*",
                            "/payments/momo/postpaybill/*",
                            "/actuator/prometheus",
                            "/users/create/*")
                    .permitAll()
                    .anyRequest().authenticated())
            .sessionManagement(ses -> ses.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilter(new JWTAuthenticationFilter(authenticationManager))
            .addFilterAfter(new JWTAuthorizationFilter(userDetailsService, authenticationManager), JWTAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public FilterRegistrationBean corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
    bean.setOrder(0);
    return bean;
  }

  CorsConfigurationSource corsConfigurationSource() {
    final var configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("*");
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("*"));
    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public BCryptPasswordEncoder getEncoder() {
    return new BCryptPasswordEncoder(10);
  }

}