package br.com.requestmngr.security;

import br.com.requestmngr.user.JdbcLocalUserRepository;
import br.com.requestmngr.user.LocalUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LocalUserDetailsService implements UserDetailsService {
    private final JdbcLocalUserRepository userRepository;
    public LocalUserDetailsService(JdbcLocalUserRepository userRepository) { this.userRepository = userRepository; }
    @Override public UserDetails loadUserByUsername(String username) {
        LocalUser user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.withUsername(user.username()).password(user.passwordHash()).authorities(new SimpleGrantedAuthority("ROLE_" + user.role().name())).disabled(!user.active()).build();
    }
}
