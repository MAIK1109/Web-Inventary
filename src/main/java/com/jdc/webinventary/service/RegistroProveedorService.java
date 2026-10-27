package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.RegistroProveedorForm;
import com.jdc.webinventary.entity.ProveedoresEntity;
import com.jdc.webinventary.repository.ProveedoresRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class RegistroProveedorService {
    private final ProveedoresRepository proveedores;

    public RegistroProveedorService(ProveedoresRepository proveedores) { this.proveedores = proveedores; }

    public List<ProveedoresEntity> listar() { return proveedores.findAll(Sort.by("nombre").ascending()); }

    public boolean identificacionRegistrada(String identificacion) {
        return identificacion != null && proveedores.existsByIdentificacionIgnoreCase(identificacion.trim());
    }

    @Transactional
    public void registrar(RegistroProveedorForm form) {
        ProveedoresEntity proveedor = new ProveedoresEntity();
        proveedor.setIdentificacion(form.getIdentificacion().trim().toUpperCase(Locale.ROOT));
        proveedor.setNombre(form.getRazonSocial().trim());
        proveedor.setTelefono(form.getTelefono().trim());
        proveedor.setCorreo(form.getCorreo().trim().toLowerCase(Locale.ROOT));
        proveedor.setDireccion(form.getDireccion().trim());
        try {
            proveedores.saveAndFlush(proveedor);
        } catch (DataIntegrityViolationException ex) {
            throw new IdentificacionDuplicadaException();
        }
    }

    public static class IdentificacionDuplicadaException extends RuntimeException {
        public IdentificacionDuplicadaException() { super("Ya existe un proveedor con esa identificación."); }
    }
}
