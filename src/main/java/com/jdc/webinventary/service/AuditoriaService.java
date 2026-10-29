package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.AuditoriasEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.AuditoriasRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuditoriaService {
    private final AuditoriasRepository auditorias;
    private final UsuariosRepository usuarios;

    public AuditoriaService(AuditoriasRepository auditorias, UsuariosRepository usuarios) {
        this.auditorias = auditorias; this.usuarios = usuarios;
    }

    @Transactional
    public void registrar(String accion, String entidad, Integer actorId) {
        if (accion == null || accion.isBlank() || entidad == null || entidad.isBlank() || actorId == null)
            throw new IllegalArgumentException("La auditoría requiere acción, registro afectado y actor.");
        UsuariosEntity actor = usuarios.findById(actorId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el actor para registrar la auditoría."));
        auditorias.save(new AuditoriasEntity(null, limitar(accion, 45), limitar(entidad, 45), LocalDate.now(), actor));
    }

    @Transactional(readOnly = true)
    public List<AuditoriasEntity> recientes() {
        return auditorias.findRecientes(PageRequest.of(0, 500));
    }

    private String limitar(String texto, int maximo) {
        String limpio = texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo);
    }
}
