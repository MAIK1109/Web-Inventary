package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.RegistroCuentaForm;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class RegistroCuentaService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificacionCorreoService verificacionCorreo;
    private final AuditoriaService auditoria;

    public RegistroCuentaService(UsuariosRepository usuariosRepository, PasswordEncoder passwordEncoder,
                                 VerificacionCorreoService verificacionCorreo, AuditoriaService auditoria) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificacionCorreo = verificacionCorreo;
        this.auditoria = auditoria;
    }

    public boolean correoYaRegistrado(String correo) {
        return usuariosRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    @Transactional
    public void reenviarVerificacion(String correo) {
        if (correo == null || correo.isBlank()) return;
        usuariosRepository.findByCorreoIgnoreCase(correo.trim())
                .filter(usuario -> !"SI".equalsIgnoreCase(usuario.getVerificado()))
                .filter(usuario -> !"BLOQUEADA".equalsIgnoreCase(usuario.getEstadoCuenta()))
                .ifPresent(verificacionCorreo::enviarEnlace);
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
        usuario.setEstadoCuenta("PENDIENTE");

        try {
            usuariosRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException();
        }
        auditoria.registrar("CREACION_CUENTA", "USUARIO#" + usuario.getIdUsuario(), usuario.getIdUsuario());
        verificacionCorreo.enviarEnlace(usuario);
    }

    public static class CorreoDuplicadoException extends RuntimeException {
        public CorreoDuplicadoException() {
            super("Ya existe una cuenta registrada con ese correo");
        }
    }
}
