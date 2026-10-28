# Web-Inventary
Sistema web integral de gestión de inventarios, compras, ventas e inversiones desarrollado en Java con Spring Boot, PostgreSQL, Spring Security y Thymeleaf.

## Administración de cuentas

El superadministrador inicial se crea al arrancar la aplicación cuando se configuran las variables de entorno `SUPERADMIN_EMAIL` y `SUPERADMIN_PASSWORD`. La contraseña debe tener al menos 12 caracteres y no debe guardarse en el repositorio. El correo debe ser nuevo en la tabla `usuarios`; el proceso no eleva automáticamente una cuenta existente a superadministrador.

En IntelliJ, configura ambas variables en **Run/Debug Configurations → Environment variables** y reinicia la aplicación. Luego inicia sesión en `/login` y administra las cuentas en `/admin/usuarios`. Las cuentas registradas públicamente reciben un enlace de verificación válido por 24 horas y quedan pendientes hasta que un superadministrador las active. Configura también `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH` y `MAIL_SMTP_STARTTLS` para enviar los enlaces, y `EMAIL_VERIFICATION_BASE_URL` con la dirección pública de la aplicación. El inicio de sesión requiere tanto el correo verificado como la cuenta activa.

## Registro de clientes

Con una sesión iniciada, abre `/clientes` para registrar identificación, nombre, teléfono y correo opcional. El consentimiento para enviar comprobantes al correo es voluntario; si se otorga, se conserva también la fecha y hora. La identificación no se puede duplicar.

## Recuperación de acceso

En la pantalla de inicio de sesión, selecciona **¿Olvidaste tu contraseña?**. Configura el envío de correo con las variables de entorno `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH` y `MAIL_SMTP_STARTTLS`; establece `PASSWORD_RESET_BASE_URL` con la dirección pública de la aplicación. El enlace usa un token de un solo uso que vence en 30 minutos. La contraseña nueva nunca se envía por correo.

## Correos de eventos

La misma configuración SMTP se usa para los enlaces de verificación y recuperación, avisos de aportes pendientes, resultados de aprobación o rechazo, comprobantes de venta y alertas de stock. Los avisos de aporte y stock se envían a administradores activos con correo verificado. Los resultados se envían al aportante; el comprobante se envía al cliente solo si autorizó recibirlo. Configura `EMAIL_VERIFICATION_BASE_URL` y `PASSWORD_RESET_BASE_URL` con la dirección pública de la aplicación.

## Registro de aportes

Los usuarios con sesión activa pueden enviar aportes desde `/aportes/mi-cuenta`. El soporte es opcional; se aceptan PDF, PNG y JPG de hasta 5 MB y se guardan en `uploads/aportes` (configurable con `APORTES_UPLOAD_DIR`). Cada aporte queda pendiente hasta que lo revise el superadministrador.

## Dashboard financiero

El dashboard está disponible para cuentas con rol de administrador o superadministrador en `/dashboard/financiero`. Permite registrar gastos y consultar ventas confirmadas, costo de mercancía, utilidad estimada, margen neto y evolución mensual. Para ventas antiguas sin costo histórico, se usa el costo de la compra confirmada más reciente anterior a la venta; si no existe un costo de referencia, esa línea se señala en el dashboard.
