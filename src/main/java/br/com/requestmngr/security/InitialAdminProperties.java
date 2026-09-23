package br.com.requestmngr.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.initial-admin")
public record InitialAdminProperties(String username, String password, String displayName) {
    public boolean isComplete() { return hasText(username) && hasText(password) && hasText(displayName); }
    private static boolean hasText(String value) { return value != null && !value.isBlank(); }
}
