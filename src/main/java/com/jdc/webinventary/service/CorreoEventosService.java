package com.jdc.webinventary.service;

import com.jdc.webinventary.entity.InversionesEntity;
import com.jdc.webinventary.entity.ProductosEntity;
import com.jdc.webinventary.entity.UsuariosEntity;
import com.jdc.webinventary.entity.VentasEntity;
import com.jdc.webinventary.repository.UsuariosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
public class CorreoEventosService {
    private static final Logger log = LoggerFactory.getLogger(CorreoEventosService.class);
    private final UsuariosRepository usuarios;
    private final ApplicationEventPublisher events;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remitente;

    public CorreoEventosService(UsuariosRepository usuarios, ApplicationEventPublisher events,
                                ObjectProvider<JavaMailSender> mailSender,
                                @Value("${spring.mail.username:}") String remitente) {
        this.usuarios = usuarios;
        this.events = events;
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void avisarNuevoAporte(InversionesEntity aporte) {
        String asunto = "Nuevo aporte pendiente de revisión";
        String mensaje = "Se registró un nuevo aporte y está pendiente de revisión.\n\n" +
                "Usuario: " + aporte.getUsuario().getNombre() + "\n" +
                "Fecha: " + aporte.getFecha() + "\n" +
                "Valor: " + aporte.getMonto() + "\n" +
                "Medio de pago: " + aporte.getMedioPago() + "\n\n" +
                "Ingresa a Web Inventary para aprobarlo o rechazarlo.";
        publicarAAdministradores(asunto, mensaje);
    }

    public void avisarResultadoAporte(InversionesEntity aporte, boolean aprobado) {
        UsuariosEntity usuario = aporte.getUsuario();
        if (!correoValido(usuario.getCorreo())) return;
        String resultado = aprobado ? "aprobado" : "rechazado";
        String detalle = "Hola " + usuario.getNombre() + ",\n\n" +
                "Tu aporte del " + aporte.getFecha() + " por valor de " + aporte.getMonto() +
                " fue " + resultado + "." +
                (aprobado ? " El valor aprobado ya se refleja en tu capital." : " Si tienes dudas, contacta al administrador.");
        publicar(usuario.getCorreo(), "Resultado de tu aporte en Web Inventary", detalle);
    }

    public void enviarComprobanteVenta(VentasEntity venta) {
        var cliente = venta.getCliente();
        if (!Boolean.TRUE.equals(cliente.getConsentimientoComprobantes()) || !correoValido(cliente.getCorreo())) return;

        StringBuilder texto = new StringBuilder()
                .append("Hola ").append(cliente.getNombre()).append(",\n\n")
                .append("Este es el comprobante de tu venta #").append(venta.getIdVenta())
                .append(" del ").append(venta.getFecha()).append(".\n\nDetalle:\n");
        long total = 0;
        for (var detalle : venta.getDetalles()) {
            long cantidad = detalle.getCantidad() == null ? 0 : detalle.getCantidad();
            long precio = detalle.getPrecioUnitario() == null ? 0 : detalle.getPrecioUnitario();
            long subtotal = Math.multiplyExact(cantidad, precio);
            total = Math.addExact(total, subtotal);
            texto.append("- ").append(detalle.getProducto().getNombre()).append(" · ")
                    .append(cantidad).append(" x ").append(precio).append(" = ").append(subtotal).append("\n");
        }
        texto.append("\nTotal: ").append(total).append("\n\nGracias por tu compra.");
        publicar(cliente.getCorreo(), "Comprobante de venta #" + venta.getIdVenta() + " - Web Inventary", texto.toString());
    }

    public void avisarAlertaStock(ProductosEntity producto, String estado) {
        if ("DISPONIBLE".equals(estado)) return;
        String asunto = "Alerta de inventario: " + producto.getNombre() + " - " + estado;
        String mensaje = "El inventario requiere atención.\n\n" +
                "Producto: " + producto.getNombre() + " (" + producto.getCodigo() + ")\n" +
                "Estado: " + estado + "\n" +
                "Existencias: " + (producto.getStockActual() == null ? 0 : producto.getStockActual()) + "\n" +
                "Stock mínimo: " + (producto.getStockMinimo() == null ? 0 : producto.getStockMinimo()) + "\n\n" +
                "Revisa el inventario en Web Inventary.";
        publicarAAdministradores(asunto, mensaje);
    }

    private void publicarAAdministradores(String asunto, String mensaje) {
        List<String> destinatarios = usuarios.findByRolIn(List.of("SUPERADMIN", "ADMINISTRADOR")).stream()
                .filter(usuario -> "ACTIVA".equals(usuario.getEstadoCuenta())
                        && "SI".equals(usuario.getVerificado()) && correoValido(usuario.getCorreo()))
                .map(UsuariosEntity::getCorreo).distinct().toList();
        destinatarios.forEach(destinatario -> publicar(destinatario, asunto, mensaje));
    }

    private boolean correoValido(String correo) {
        return correo != null && !correo.isBlank();
    }

    private void publicar(String destinatario, String asunto, String mensaje) {
        events.publishEvent(new CorreoEvento(destinatario, asunto, mensaje));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(CorreoEvento evento) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No se envió el correo de evento porque el servidor SMTP no está configurado.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (!remitente.isBlank()) message.setFrom(remitente);
        message.setTo(evento.destinatario());
        message.setSubject(evento.asunto());
        message.setText(evento.mensaje());
        try {
            sender.send(message);
            log.info("Correo de evento '{}' enviado a {}.", evento.asunto(), evento.destinatario());
        } catch (MailException ex) {
            log.error("No fue posible enviar el correo de evento '{}' a {}.",
                    evento.asunto(), evento.destinatario(), ex);
        }
    }

    public record CorreoEvento(String destinatario, String asunto, String mensaje) { }
}
