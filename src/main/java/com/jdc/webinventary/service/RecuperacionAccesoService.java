package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.TokenRecuperacionEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.TokenRecuperacionRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RecuperacionAccesoService {
    private static final Logger log = LoggerFactory.getLogger(RecuperacionAccesoService.class);
    private static final int VIGENCIA_MINUTOS = 30;
    private final UsuariosRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String baseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public RecuperacionAccesoService(UsuariosRepository usuarios, TokenRecuperacionRepository tokens,
                                     PasswordEncoder passwordEncoder, ObjectProvider<JavaMailSender> mailSender,
                                     @Value("${app.security.password-reset-base-url}") String baseUrl) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    @Transactional
    public void solicitar(String correo) {
        if (correo == null || correo.isBlank()) return;
        usuarios.findByCorreoIgnoreCase(correo.trim())
                .filter(usuario -> usuario.getEstadoCuenta() == null || "ACTIVA".equals(usuario.getEstadoCuenta()))
                .ifPresent(usuario -> emitirToken(usuario, correo.trim()));
    }

    private void emitirToken(UsuariosEntity usuario, String correo) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime ahora = LocalDateTime.now();
        tokens.invalidarActivosUsuario(usuario.getIdUsuario(), ahora);
        tokens.save(new TokenRecuperacionEntity(hash(tokenPlano), ahora.plusMinutes(VIGENCIA_MINUTOS), usuario));

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No se envió el correo de recuperación porque el servidor SMTP no está configurado.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(correo);
        message.setSubject("Recuperación de acceso a Web Inventary");
        message.setText("Hola " + usuario.getNombre() + ",\n\n" +
                "Usa este enlace para crear una contraseña nueva. El enlace vence en " + VIGENCIA_MINUTOS +
                " minutos y solo puede usarse una vez:\n\n" + baseUrl + "/recuperar-acceso/nueva?token=" +
                tokenPlano + "\n\nSi no solicitaste este cambio, puedes ignorar este mensaje. No respondas con tu contraseña.");
        try {
            sender.send(message);
        } catch (MailException ex) {
            log.error("No fue posible enviar un correo de recuperación.", ex);
        }
    }

    @Transactional
    public boolean cambiarPassword(String tokenPlano, String nuevaPassword) {
        if (tokenPlano == null || tokenPlano.isBlank() || nuevaPassword == null) return false;
        LocalDateTime ahora = LocalDateTime.now();
        TokenRecuperacionEntity token = tokens.buscarVigente(hash(tokenPlano), ahora).orElse(null);
        if (token == null || tokens.consumirSiVigente(token.getTokenHash(), ahora) != 1) return false;
        UsuariosEntity usuario = token.getUsuario();
        usuario.setHashPassword(passwordEncoder.encode(nuevaPassword));
        usuarios.save(usuario);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean tokenVigente(String tokenPlano) {
        return tokenPlano != null && !tokenPlano.isBlank()
                && tokens.buscarVigente(hash(tokenPlano), LocalDateTime.now()).isPresent();
    }

    private String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }
}
