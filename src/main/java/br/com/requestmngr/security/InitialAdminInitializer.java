package br.com.requestmngr.security;

import br.com.requestmngr.user.JdbcLocalUserRepository;
import br.com.requestmngr.user.LocalUser;
import br.com.requestmngr.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class InitialAdminInitializer implements ApplicationRunner {
    private final JdbcLocalUserRepository userRepository;
    private final InitialAdminProperties properties;
    private final PasswordEncoder passwordEncoder;
    public InitialAdminInitializer(JdbcLocalUserRepository userRepository, InitialAdminProperties properties, PasswordEncoder passwordEncoder) { this.userRepository = userRepository; this.properties = properties; this.passwordEncoder = passwordEncoder; }
    @Override public void run(ApplicationArguments args) {
        if (userRepository.hasAnyUser()) return;
        if (!properties.isComplete()) throw new IllegalStateException("Configure APP_INITIAL_ADMIN_USERNAME, APP_INITIAL_ADMIN_PASSWORD and APP_INITIAL_ADMIN_DISPLAY_NAME before the first startup.");
        userRepository.insert(new LocalUser(null, properties.username(), passwordEncoder.encode(properties.password()), properties.displayName(), UserRole.ADMIN, true));
    }
}
