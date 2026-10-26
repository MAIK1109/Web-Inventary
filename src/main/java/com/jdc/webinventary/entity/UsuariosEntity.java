package com.jdc.webinventary.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_correo", columnNames = "correo")
})
public class UsuariosEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "identificacion", length = 45)
    private String identificacion;

    @Column(name = "nombre", length = 45)
    private String nombre;

    @Column(name = "telefono", length = 45)
    private String telefono;

    @Column(name = "correo", length = 254, nullable = false)
    private String correo;

    @Column(name = "rol", length = 45)
    private String rol;

    @Column(name = "verificado", length = 45)
    private String verificado;

    @Column(name = "hash_password", length = 120)
    private String hashPassword;

    // Relación 1:N con InversionesEntity (Usuario_idUsuario)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InversionesEntity> inversiones = new ArrayList<>();

    // Relación 1:N con AuditoriasEntity (Usuario_idUsuario)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditoriasEntity> auditorias = new ArrayList<>();

    // Relación 1:N con MovimientosInventariosEntity (Usuario_idUsuario)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientosInventariosEntity> movimientosInventarios = new ArrayList<>();

    public UsuariosEntity() {
    }

    public UsuariosEntity(Integer idUsuario, String identificacion, String nombre, String correo,
                          String rol, String verificado, String hashPassword) {
        this.idUsuario = idUsuario;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.verificado = verificado;
        this.hashPassword = hashPassword;
    }

    // Métodos de conveniencia para la relación bidireccional con Inversiones
    public void addInversion(InversionesEntity inversion) {
        inversiones.add(inversion);
        inversion.setUsuario(this);
    }

    public void removeInversion(InversionesEntity inversion) {
        inversiones.remove(inversion);
        inversion.setUsuario(null);
    }

    // Métodos de conveniencia para la relación bidireccional con Auditorías
    public void addAuditoria(AuditoriasEntity auditoria) {
        auditorias.add(auditoria);
        auditoria.setUsuario(this);
    }

    public void removeAuditoria(AuditoriasEntity auditoria) {
        auditorias.remove(auditoria);
        auditoria.setUsuario(null);
    }

    // Métodos de conveniencia para la relación bidireccional con Movimientos de Inventario
    public void addMovimientoInventario(MovimientosInventariosEntity movimiento) {
        movimientosInventarios.add(movimiento);
        movimiento.setUsuario(this);
    }

    public void removeMovimientoInventario(MovimientosInventariosEntity movimiento) {
        movimientosInventarios.remove(movimiento);
        movimiento.setUsuario(null);
    }

    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getVerificado() {
        return verificado;
    }

    public void setVerificado(String verificado) {
        this.verificado = verificado;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public List<InversionesEntity> getInversiones() {
        return inversiones;
    }

    public void setInversiones(List<InversionesEntity> inversiones) {
        this.inversiones = inversiones;
    }

    public List<AuditoriasEntity> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriasEntity> auditorias) {
        this.auditorias = auditorias;
    }

    public List<MovimientosInventariosEntity> getMovimientosInventarios() {
        return movimientosInventarios;
    }

    public void setMovimientosInventarios(List<MovimientosInventariosEntity> movimientosInventarios) {
        this.movimientosInventarios = movimientosInventarios;
    }
}
