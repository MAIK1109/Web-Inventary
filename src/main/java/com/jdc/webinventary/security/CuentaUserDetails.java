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
    private final String verificado;
    private final Collection<? extends GrantedAuthority> authorities;

    public CuentaUserDetails(UsuariosEntity usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.correo = usuario.getCorreo();
        this.hashPassword = usuario.getHashPassword();
        this.estadoCuenta = usuario.getEstadoCuenta();
        this.verificado = usuario.getVerificado();

        String rol = usuario.getRol();
        if (rol == null || rol.isBlank() || "USUARIO".equals(rol)) rol = "VENDEDOR_INVERSIONISTA";
        if (!List.of("SUPERADMIN", "ADMINISTRADOR", "VENDEDOR_INVERSIONISTA").contains(rol))
            rol = "VENDEDOR_INVERSIONISTA";
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
        return "ACTIVA".equals(estadoCuenta) && "SI".equals(verificado);
    }
}
