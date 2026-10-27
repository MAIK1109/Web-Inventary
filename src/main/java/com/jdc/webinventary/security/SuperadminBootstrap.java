package com.jdc.webinventary.security;

import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SuperadminBootstrap implements ApplicationRunner {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correo;
    private final String password;

    public SuperadminBootstrap(UsuariosRepository usuariosRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${SUPERADMIN_EMAIL:}") String correo,
                               @Value("${SUPERADMIN_PASSWORD:}") String password) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (correo.isBlank() && password.isBlank()) {
            return;
        }
        if (correo.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Configura SUPERADMIN_EMAIL y una SUPERADMIN_PASSWORD de al menos 12 caracteres");
        }

        var existing = usuariosRepository.findByCorreoIgnoreCase(correo);
        if (existing.isPresent()) {
            UsuariosEntity superadmin = existing.get();
            superadmin.setHashPassword(passwordEncoder.encode(password));
            superadmin.setRol("SUPERADMIN");
            superadmin.setVerificado("SI");
            superadmin.setEstadoCuenta("ACTIVA");
            usuariosRepository.save(superadmin);
            return;
        }

        UsuariosEntity superadmin = new UsuariosEntity();
        superadmin.setIdentificacion("SUPERADMIN-INICIAL");
        superadmin.setNombre("Superadministrador");
        superadmin.setTelefono("No registrado");
        superadmin.setCorreo(correo);
        superadmin.setHashPassword(passwordEncoder.encode(password));
        superadmin.setRol("SUPERADMIN");
        superadmin.setVerificado("SI");
        superadmin.setEstadoCuenta("ACTIVA");
        usuariosRepository.save(superadmin);
    }
}
