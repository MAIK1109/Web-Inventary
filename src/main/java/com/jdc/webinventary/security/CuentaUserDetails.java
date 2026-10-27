package com.jdc.webinventary.security;

import com.jdc.webinventary.entity.UsuariosEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CuentaUserDetails implements UserDetails {

    private final Integer idUsuario;
    private final String correo;
    private final String hashPassword;
    private final String estadoCuenta;
    private final Collection<? extends GrantedAuthority> authorities;

    public CuentaUserDetails(UsuariosEntity usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.correo = usuario.getCorreo();
        this.hashPassword = usuario.getHashPassword();
        this.estadoCuenta = usuario.getEstadoCuenta();

        String rol = usuario.getRol();
        if (rol == null || !List.of("SUPERADMIN", "ADMINISTRADOR", "USUARIO").contains(rol)) {
            rol = "USUARIO";
        }
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return hashPassword;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"BLOQUEADA".equals(estadoCuenta);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estadoCuenta == null || "ACTIVA".equals(estadoCuenta);
    }
}
