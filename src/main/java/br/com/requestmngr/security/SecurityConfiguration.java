package br.com.requestmngr.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableConfigurationProperties(InitialAdminProperties.class)
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");

        http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login.html", "/login.js", "/styles.css", "/api/csrf", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/purchase-requests").hasAnyRole("ADMIN", "REQUESTER")
                        .requestMatchers(HttpMethod.PUT, "/api/purchase-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/purchase-requests/**", "/api/auth/me").hasAnyRole("ADMIN", "REQUESTER", "BUYER", "MANAGER")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .failureHandler((request, response, exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value())))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value())))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), request -> request.getRequestURI().startsWith("/api/"))
                        .defaultAccessDeniedHandlerFor((request, response, exception) -> response.sendError(HttpStatus.FORBIDDEN.value()), request -> request.getRequestURI().startsWith("/api/")));

        return http.build();
    }
}
