# Web-Inventary
Sistema web integral de gestión de inventarios, compras, ventas e inversiones desarrollado en Java con Spring Boot, PostgreSQL, Spring Security y Thymeleaf.

## Administración de cuentas

El superadministrador inicial se crea al arrancar la aplicación cuando se configuran las variables de entorno `SUPERADMIN_EMAIL` y `SUPERADMIN_PASSWORD`. La contraseña debe tener al menos 12 caracteres y no debe guardarse en el repositorio. El correo debe ser nuevo en la tabla `usuarios`; el proceso no eleva automáticamente una cuenta existente a superadministrador.

En IntelliJ, configura ambas variables en **Run/Debug Configurations → Environment variables** y reinicia la aplicación. Luego inicia sesión en `/login` y administra las cuentas en `/admin/usuarios`. Las cuentas registradas públicamente quedan pendientes hasta que un superadministrador las active.

## Registro de clientes

Con una sesión iniciada, abre `/clientes` para registrar identificación, nombre, teléfono y correo opcional. El consentimiento para enviar comprobantes al correo es voluntario; si se otorga, se conserva también la fecha y hora. La identificación no se puede duplicar.
