# Web-Inventary
Sistema web integral de gestión de inventarios, compras, ventas e inversiones desarrollado en Java con Spring Boot, PostgreSQL, Spring Security y Thymeleaf.

## Administración de cuentas

El superadministrador inicial se crea al arrancar la aplicación cuando se configuran las variables de entorno `SUPERADMIN_EMAIL` y `SUPERADMIN_PASSWORD`. La contraseña debe tener al menos 12 caracteres y no debe guardarse en el repositorio. El correo debe ser nuevo en la tabla `usuarios`; el proceso no eleva automáticamente una cuenta existente a superadministrador.

En IntelliJ, configura ambas variables en **Run/Debug Configurations → Environment variables** y reinicia la aplicación. Luego inicia sesión en `/login` y administra las cuentas en `/admin/usuarios`. Las cuentas registradas públicamente quedan pendientes hasta que un superadministrador las active.

## Registro de clientes

Con una sesión iniciada, abre `/clientes` para registrar identificación, nombre, teléfono y correo opcional. El consentimiento para enviar comprobantes al correo es voluntario; si se otorga, se conserva también la fecha y hora. La identificación no se puede duplicar.

## Recuperación de acceso

En la pantalla de inicio de sesión, selecciona **¿Olvidaste tu contraseña?**. Configura el envío de correo con las variables de entorno `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH` y `MAIL_SMTP_STARTTLS`; establece `PASSWORD_RESET_BASE_URL` con la dirección pública de la aplicación. El enlace usa un token de un solo uso que vence en 30 minutos. La contraseña nueva nunca se envía por correo.

## Registro de aportes

Los usuarios con sesión activa pueden enviar aportes desde `/aportes/mi-cuenta`. El soporte es opcional; se aceptan PDF, PNG y JPG de hasta 5 MB y se guardan en `uploads/aportes` (configurable con `APORTES_UPLOAD_DIR`). Cada aporte queda pendiente hasta que lo revise el superadministrador.
