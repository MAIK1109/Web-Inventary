package com.jdc.webinventary.security;

import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CuentaUserDetailsService implements UserDetailsService {

    private final UsuariosRepository usuariosRepository;

    public CuentaUserDetailsService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuariosRepository.findByCorreoIgnoreCase(correo.trim())
                .map(CuentaUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo"));
    }
}
