package com.jdc.webinventary.service;

import com.jdc.webinventary.dto.RegistroClienteForm;
import com.jdc.webinventary.entity.ClientesEntity;
import com.jdc.webinventary.repository.ClientesRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class RegistroClienteService {

    private final ClientesRepository clientesRepository;

    public RegistroClienteService(ClientesRepository clientesRepository) {
        this.clientesRepository = clientesRepository;
    }

    public boolean identificacionRegistrada(String identificacion) {
        return clientesRepository.existsByIdentificacionIgnoreCase(identificacion.trim());
    }

    @Transactional
    public void registrar(RegistroClienteForm form) {
        ClientesEntity cliente = new ClientesEntity();
        cliente.setIdentificacion(form.getIdentificacion().trim().toUpperCase(Locale.ROOT));
        cliente.setNombre(form.getNombre().trim());
        cliente.setTelefono(form.getTelefono().trim());
        cliente.setCorreo(form.getCorreo() == null || form.getCorreo().isBlank()
                ? null : form.getCorreo().trim().toLowerCase(Locale.ROOT));
        cliente.setConsentimientoComprobantes(form.isConsentimientoComprobantes());
        cliente.setFechaConsentimientoComprobantes(form.isConsentimientoComprobantes()
                ? LocalDateTime.now() : null);

        try {
            clientesRepository.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException ex) {
            throw new IdentificacionDuplicadaException();
        }
    }

    public static class IdentificacionDuplicadaException extends RuntimeException {
        public IdentificacionDuplicadaException() {
            super("Ya existe un cliente con esa identificación");
        }
    }
}
