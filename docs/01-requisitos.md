# Requisitos y alcance

NeighborLoop es el proyecto de Android del grupo Git Happens para la evaluación de Firebase y MQTT. La implementación utiliza Kotlin y Jetpack Compose.

| Área | Alcance | Estado |
|---|---|---|
| Cuenta y perfil | Registro, acceso, recuperación, perfil y vivienda alfanumérica | Implementado. Acceso con Google y persistencia de perfil verificados en teléfono |
| Permisos | Pertenencia por comunidad y roles de residente y administrador | Implementados mediante claims y reglas. Validación de permisos pendiente |
| Chat | Identidad, vivienda, contenido, fecha y actualización en tiempo real | Verificado entre dos dispositivos |
| Avisos | Lectura por miembros y publicación por administradores | Implementado. Falta verificar publicación con un administrador |
| FCM | Notificación al publicar un aviso y apertura por ID | Implementado. Despliegue y aceptación pendientes |
| MQTT | Alertas confirmadas, TLS, QoS 1, reintento y deduplicación | Envío, recepción y reconexión verificados |
| Historial | Registro de alertas y cambio de estado por administrador | Registro verificado. Atención y cierre entre dispositivos pendientes |
| Creatividad | Galería de espacios, demostración y estado de envío | Implementados. La galería requiere validación en dispositivo |
| Compraventa | Funcionalidad mencionada en el enunciado | Alcance pendiente de confirmar con la rúbrica |
| Calidad | Pruebas de seguridad, accesibilidad, usabilidad y rendimiento | Validación pendiente |
