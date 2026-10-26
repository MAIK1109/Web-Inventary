package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.RegistroCuentaForm;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class RegistroCuentaService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public RegistroCuentaService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    public boolean correoYaRegistrado(String correo) {
        return usuariosRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    @Transactional
    public void registrar(RegistroCuentaForm form) {
        UsuariosEntity usuario = new UsuariosEntity();
        usuario.setIdentificacion(form.getIdentificacion().trim());
        usuario.setNombre(form.getNombre().trim());
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setCorreo(form.getCorreo().trim().toLowerCase(Locale.ROOT));
        usuario.setHashPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setRol("USUARIO");
        usuario.setVerificado("NO");

        try {
            usuariosRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException();
        }
    }

    public static class CorreoDuplicadoException extends RuntimeException {
        public CorreoDuplicadoException() {
            super("Ya existe una cuenta registrada con ese correo");
        }
    }
}
