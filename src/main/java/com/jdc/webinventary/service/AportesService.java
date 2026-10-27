package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.InversionesEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.InversionesRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AportesService {
    public static final byte PENDIENTE = 0;
    public static final byte APROBADO = 1;
    public static final byte RECHAZADO = 2;

    private final InversionesRepository inversiones;
    private final UsuariosRepository usuarios;

    public AportesService(InversionesRepository inversiones, UsuariosRepository usuarios) {
        this.inversiones = inversiones;
        this.usuarios = usuarios;
    }

    public List<InversionesEntity> listarPendientes() {
        return inversiones.findByEstadoOrderByFechaDesc(PENDIENTE);
    }

    public List<InversionesEntity> listarAportes() {
        return inversiones.findAllByOrderByFechaDesc();
    }

    public long totalGeneral() {
        return inversiones.totalPorEstado(APROBADO);
    }

    public long totalUsuario(Integer usuarioId) {
        return inversiones.totalUsuarioPorEstado(APROBADO, usuarioId);
    }

    public Map<Integer, Long> totalesPorUsuario() {
        return inversiones.totalesAprobadosPorUsuario(APROBADO);
    }

    @Transactional
    public void registrar(Integer usuarioId, int monto, String medioPago) {
        if (monto <= 0) throw new IllegalArgumentException("El monto del aporte debe ser mayor que cero.");
        UsuariosEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        inversiones.save(new InversionesEntity(null, monto, LocalDate.now(), medioPago,
                PENDIENTE, null, usuario));
    }

    public List<InversionesEntity> listarUsuario(Integer usuarioId) {
        return inversiones.findByUsuario_IdUsuarioOrderByFechaDesc(usuarioId);
    }

    @Transactional
    public void decidir(Integer id, boolean aprobar, String superadmin) {
        int actualizados = inversiones.decidirSiPendiente(id, PENDIENTE,
                aprobar ? APROBADO : RECHAZADO, aprobar ? superadmin : null);
        if (actualizados == 0) {
            throw new AporteNoEncontradoException("El aporte no existe o ya fue revisado.");
        }
    }

    public static class AporteNoEncontradoException extends RuntimeException {
        public AporteNoEncontradoException(String mensaje) { super(mensaje); }
    }
}
