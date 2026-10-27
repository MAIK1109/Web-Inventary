package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_verificacion", indexes = {
        @Index(name = "idx_token_verificacion_hash", columnList = "token_hash", unique = true)
})
public class TokenVerificacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtoken")
    private Integer id;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "consumido_en")
    private LocalDateTime consumidoEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_idusuario", nullable = false)
    private UsuariosEntity usuario;

    protected TokenVerificacionEntity() { }

    public TokenVerificacionEntity(String tokenHash, LocalDateTime expiraEn, UsuariosEntity usuario) {
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
        this.usuario = usuario;
    }

    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public UsuariosEntity getUsuario() { return usuario; }
}
