package com.jdc.webinventary.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        Pbkdf2PasswordEncoder legacy = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        return new PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) { return bcrypt.encode(rawPassword); }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) return false;
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")
                        || encodedPassword.startsWith("$2y$")) return bcrypt.matches(rawPassword, encodedPassword);
                // Existing accounts keep working while newly saved passwords use BCrypt.
                try { return legacy.matches(rawPassword, encodedPassword); }
                catch (IllegalArgumentException ex) { return false; }
            }
            @Override public boolean upgradeEncoding(String encodedPassword) {
                return encodedPassword == null || !(encodedPassword.startsWith("$2a$")
                        || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$"));
            }
        };
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/registro", "/registro/**", "/login", "/verificar-correo", "/recuperar-acceso/**", "/error", "/images/**", "/css/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("SUPERADMIN")

                        .requestMatchers("/reportes", "/reportes/**").hasRole("SUPERADMIN")
                        .requestMatchers("/dashboard/financiero", "/dashboard/financiero/**")
                        .hasAnyRole("SUPERADMIN", "ADMINISTRADOR")

                        .requestMatchers("/dashboard/financiero", "/dashboard/financiero/**",
                                "/dashboard/comercial", "/dashboard/comercial/**",
                                "/dashboard/inventario", "/dashboard/inventario/**",
                                "/compras", "/compras/**", "/proveedores", "/proveedores/**")
                        .hasRole("SUPERADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/productos/**")
                        .hasRole("SUPERADMIN")

                        .requestMatchers("/cuenta/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                );
        return http.build();
    }
}
