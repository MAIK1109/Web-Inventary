package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.TokenVerificacionEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.TokenVerificacionRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
public class VerificacionCorreoService {
    private static final Logger log = LoggerFactory.getLogger(VerificacionCorreoService.class);
    private static final int VIGENCIA_HORAS = 24;
    private final UsuariosRepository usuarios;
    private final TokenVerificacionRepository tokens;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String baseUrl;
    private final String remitente;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificacionCorreoService(UsuariosRepository usuarios, TokenVerificacionRepository tokens,
                                     ObjectProvider<JavaMailSender> mailSender,
                                     @Value("${app.security.email-verification-base-url:http://localhost:8086}") String baseUrl,
                                     @Value("${spring.mail.username:}") String remitente) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.remitente = remitente;
    }

    @Transactional
    public void enviarEnlace(UsuariosEntity usuario) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime ahora = LocalDateTime.now();
        tokens.invalidarActivosUsuario(usuario.getIdUsuario(), ahora);
        tokens.save(new TokenVerificacionEntity(hash(tokenPlano), ahora.plusHours(VIGENCIA_HORAS), usuario));

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No se envió el correo de verificación porque el servidor SMTP no está configurado.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (!remitente.isBlank()) message.setFrom(remitente);
        message.setTo(usuario.getCorreo());
        message.setSubject("Verifica tu correo para Web Inventary");
        message.setText("Hola " + usuario.getNombre() + ",\n\n" +
                "Confirma tu correo usando este enlace. El enlace vence en " + VIGENCIA_HORAS +
                " horas y solo puede usarse una vez:\n\n" + baseUrl + "/verificar-correo?token=" + tokenPlano +
                "\n\nSi no creaste esta cuenta, puedes ignorar este mensaje.");
        try {
            sender.send(message);
        } catch (MailException ex) {
            log.error("No fue posible enviar el correo de verificación.", ex);
        }
    }

    @Transactional
    public boolean verificar(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isBlank()) return false;
        LocalDateTime ahora = LocalDateTime.now();
        TokenVerificacionEntity token = tokens.buscarVigente(hash(tokenPlano), ahora).orElse(null);
        if (token == null || tokens.consumirSiVigente(token.getTokenHash(), ahora) != 1) return false;

        UsuariosEntity usuario = token.getUsuario();
        usuario.setVerificado("SI");
        usuarios.save(usuario);
        return true;
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
