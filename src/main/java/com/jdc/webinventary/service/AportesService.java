package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.InversionesEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.repository.InversionesRepository;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
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
    private final AporteSoporteStorageService soporteStorage;
    private final CorreoEventosService correoEventos;
    private final AuditoriaService auditoria;

    public AportesService(InversionesRepository inversiones, UsuariosRepository usuarios,
                          AporteSoporteStorageService soporteStorage, CorreoEventosService correoEventos,
                          AuditoriaService auditoria) {
        this.inversiones = inversiones;
        this.usuarios = usuarios;
        this.soporteStorage = soporteStorage;
        this.correoEventos = correoEventos;
        this.auditoria = auditoria;
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
        registrar(usuarioId, monto, LocalDate.now(), medioPago, null);
    }

    @Transactional
    public void registrar(Integer usuarioId, Integer monto, LocalDate fecha,
                          String medioPago, MultipartFile soporte) {
        registrar(usuarioId, monto, fecha, medioPago, soporte, true, null);
    }

    @Transactional
    public void registrar(Integer usuarioId, Integer monto, LocalDate fecha,
                          String medioPago, MultipartFile soporte,
                          boolean requiereAprobacion, String aprobador) {
        if (monto == null || monto <= 0) throw new IllegalArgumentException("El monto del aporte debe ser mayor que cero.");
        if (fecha == null) throw new IllegalArgumentException("La fecha del aporte es obligatoria.");
        if (medioPago == null || medioPago.isBlank() || medioPago.length() > 45) {
            throw new IllegalArgumentException("Selecciona un medio de pago válido.");
        }
        if (!requiereAprobacion && (aprobador == null || aprobador.isBlank())) {
            throw new IllegalArgumentException("Se requiere identificar al superadministrador que registra el aporte.");
        }
        UsuariosEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        String archivo = soporteStorage.guardar(soporte);
        try {
            InversionesEntity aporte = new InversionesEntity(null, monto, fecha, medioPago.trim(),
                    requiereAprobacion ? PENDIENTE : APROBADO, requiereAprobacion ? null : aprobador, usuario);
            aporte.setSoporteArchivo(archivo);
            inversiones.saveAndFlush(aporte);
            auditoria.registrar("REGISTRO_APORTE", "APORTE#" + aporte.getIdInversion(), usuarioId);
            if (requiereAprobacion) {
                correoEventos.avisarNuevoAporte(aporte);
            } else {
                auditoria.registrar("APROBACION_APORTE",
                        "APORTE#" + aporte.getIdInversion() + ":APROBADO", usuarioId);
            }
        } catch (RuntimeException ex) {
            soporteStorage.eliminar(archivo);
            throw ex;
        }
    }

    public List<InversionesEntity> listarUsuario(Integer usuarioId) {
        return inversiones.findByUsuario_IdUsuarioOrderByFechaDesc(usuarioId);
    }

    public Resource soporteParaUsuario(Integer aporteId, Integer usuarioId) {
        InversionesEntity aporte = inversiones.findByIdInversionAndUsuario_IdUsuario(aporteId, usuarioId)
                .orElseThrow(AporteNoEncontradoException::new);
        return cargarSoporte(aporte);
    }

    public Resource soporteParaRevision(Integer aporteId) {
        InversionesEntity aporte = inversiones.findById(aporteId)
                .orElseThrow(AporteNoEncontradoException::new);
        return cargarSoporte(aporte);
    }

    private Resource cargarSoporte(InversionesEntity aporte) {
        if (aporte.getSoporteArchivo() == null) throw new AporteNoEncontradoException();
        try {
            return soporteStorage.cargar(aporte.getSoporteArchivo());
        } catch (AporteSoporteStorageService.AporteSoporteNoEncontradoException ex) {
            throw new AporteNoEncontradoException();
        }
    }

    @Transactional
    public void decidir(Integer id, boolean aprobar, String superadmin) {
        InversionesEntity aporte = inversiones.findByIdWithUsuario(id)
                .orElseThrow(() -> new AporteNoEncontradoException("El aporte no existe o ya fue revisado."));
        int actualizados = inversiones.decidirSiPendiente(id, PENDIENTE,
                aprobar ? APROBADO : RECHAZADO, aprobar ? superadmin : null);
        if (actualizados == 0) {
            throw new AporteNoEncontradoException("El aporte no existe o ya fue revisado.");
        }
        UsuariosEntity actor = usuarios.findByCorreoIgnoreCase(superadmin)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el actor de la aprobación."));
        auditoria.registrar(aprobar ? "APROBACION_APORTE" : "RECHAZO_APORTE",
                "APORTE#" + id + (aprobar ? ":APROBADO" : ":RECHAZADO"), actor.getIdUsuario());
        correoEventos.avisarResultadoAporte(aporte, aprobar);
    }

    public static class AporteNoEncontradoException extends RuntimeException {
        public AporteNoEncontradoException(String mensaje) { super(mensaje); }
        public AporteNoEncontradoException() { super("El aporte o su soporte no existe."); }
    }
}
