package nl.appall.spring.security336.testsecurityspring336;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Or any other PasswordEncoder implementation
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pw) {
        var hjh = User.withUsername("hjh").password(pw.encode("Password123!")).roles("ADMIN").build();
        var guest = User.withUsername("guest").password(pw.encode("Guest123!")).roles("GUEST").build();
        var editor = User.withUsername("editor").password(pw.encode("Editor123!")).roles("EDITOR").build();

        return new InMemoryUserDetailsManager(hjh, guest, editor);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers("/admin").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .oneTimeTokenLogin(configurer ->
                        configurer.tokenGenerationSuccessHandler(((request, response, oneTimeToken) -> {
                    var msg = "go to http://localhost:8080/login/ott?token="+ oneTimeToken.getTokenValue();
                    System.out.println(msg); // Send mail, or notification
                    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                    response.getWriter().print("you've got console mail...");
                })))
                .webAuthn( c ->
                        c.rpId("localhost")
                                .rpName("bootifull psswrd")
                                .allowedOrigins("http://localhost:8080")
                )
                .formLogin(Customizer.withDefaults()) // !!
                .build();
    }
}
