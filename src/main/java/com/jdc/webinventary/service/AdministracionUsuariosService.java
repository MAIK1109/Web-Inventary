package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.AdministrarCuentaForm;
import com.jdc.webinventary.dto.CuentaPerfilForm;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.UsuariosRepository;
import com.jdc.webinventary.security.CuentaUserDetails;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdministracionUsuariosService {

    private final UsuariosRepository usuariosRepository;
    private final SessionRegistry sessionRegistry;

    public AdministracionUsuariosService(UsuariosRepository usuariosRepository, SessionRegistry sessionRegistry) {
        this.usuariosRepository = usuariosRepository;
        this.sessionRegistry = sessionRegistry;
    }

    public List<UsuariosEntity> listarUsuarios() {
        return usuariosRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    public UsuariosEntity obtenerUsuario(Integer idUsuario) {
        return usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException());
    }

    @Transactional
    public void actualizarPerfil(Integer idUsuario, CuentaPerfilForm form) {
        UsuariosEntity usuario = obtenerUsuario(idUsuario);
        String correo = normalizarCorreo(form.getCorreo());
        validarCorreoDisponible(correo, idUsuario);

        usuario.setIdentificacion(form.getIdentificacion().trim());
        usuario.setNombre(form.getNombre().trim());
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setCorreo(correo);
        try {
            usuariosRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException();
        }
    }

    @Transactional
    public void actualizarComoSuperadmin(Integer idUsuario, AdministrarCuentaForm form) {
        UsuariosEntity usuario = obtenerUsuario(idUsuario);
        String correo = normalizarCorreo(form.getCorreo());
        validarCorreoDisponible(correo, idUsuario);

        boolean cambiaRol = !form.getRol().equals(usuario.getRol());
        boolean cambiaCorreo = !correo.equalsIgnoreCase(usuario.getCorreo());
        if ("SUPERADMIN".equals(form.getRol()) && !"SUPERADMIN".equals(usuario.getRol())) {
            throw new OperacionNoPermitidaException("No se puede asignar el rol de superadministrador desde la administración de usuarios");
        }
        if ("SUPERADMIN".equals(usuario.getRol()) && cambiaRol) {
            throw new OperacionNoPermitidaException("No se puede cambiar el rol del superadministrador");
        }
        usuario.setIdentificacion(form.getIdentificacion().trim());
        usuario.setNombre(form.getNombre().trim());
        usuario.setTelefono(form.getTelefono().trim());
        usuario.setCorreo(correo);
        usuario.setRol(form.getRol());
        try {
            usuariosRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new CorreoDuplicadoException();
        }

        if (cambiaRol || cambiaCorreo) {
            expirarSesiones(idUsuario);
        }
    }

    @Transactional
    public void activar(Integer idUsuario) {
        UsuariosEntity usuario = obtenerUsuario(idUsuario);
        protegerSuperadmin(usuario);
        usuario.setEstadoCuenta("ACTIVA");
        usuario.setVerificado("SI");
        usuariosRepository.saveAndFlush(usuario);
        expirarSesiones(idUsuario);
    }

    @Transactional
    public void bloquear(Integer idUsuario, String correoActor) {
        UsuariosEntity usuario = obtenerUsuario(idUsuario);
        protegerSuperadmin(usuario);
        if (usuario.getCorreo().equalsIgnoreCase(correoActor)) {
            throw new OperacionNoPermitidaException("No puedes bloquear tu propia cuenta de superadministrador");
        }
        usuario.setEstadoCuenta("BLOQUEADA");
        usuariosRepository.saveAndFlush(usuario);
        expirarSesiones(idUsuario);
    }

    private void protegerSuperadmin(UsuariosEntity usuario) {
        if ("SUPERADMIN".equals(usuario.getRol())) {
            throw new OperacionNoPermitidaException("La cuenta del superadministrador no se puede activar o bloquear desde esta pantalla");
        }
    }

    private void validarCorreoDisponible(String correo, Integer idUsuario) {
        if (usuariosRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(correo, idUsuario)) {
            throw new CorreoDuplicadoException();
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private void expirarSesiones(Integer idUsuario) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof CuentaUserDetails details && idUsuario.equals(details.getIdUsuario())) {
                for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                    session.expireNow();
                }
            }
        }
    }

    public static class CorreoDuplicadoException extends RuntimeException {
        public CorreoDuplicadoException() { super("Este correo ya está registrado en otra cuenta"); }
    }

    public static class UsuarioNoEncontradoException extends RuntimeException {
        public UsuarioNoEncontradoException() { super("No se encontró la cuenta solicitada"); }
    }

    public static class OperacionNoPermitidaException extends RuntimeException {
        public OperacionNoPermitidaException(String mensaje) { super(mensaje); }
    }
}
