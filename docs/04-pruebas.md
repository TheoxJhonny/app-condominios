# Pruebas y resultados

## Resultados registrados

- Android Studio ejecutó `:app:testDebugUnitTest :app:assembleDebug` con resultado exitoso. Los informes de JUnit registran 14 pruebas unitarias aprobadas, siete de `CommunityTest` y siete de `AlertEventValidatorTest`.
- El 21 de noviembre de 2026 se verificaron el arranque y la demostración en emulador, el acceso con Google, la persistencia del perfil, el chat entre teléfono y emulador y la recepción, registro y reconexión de alertas con EMQX Serverless.
- El rediseño se comprobó mediante compilación directa de Kotlin. La revisión del carrusel y del diseño en dispositivo sigue pendiente.

## Matriz de aceptación

| ID | Condiciones y pasos | Esperado | Estado |
|---|---|---|---|
| REG-01 | Registro con todos los campos vacíos | Errores; no crear cuenta | Validación implementada; prueba manual pendiente |
| REG-02 | Registro con vivienda 311A | Aceptar; conservar vivienda | Unidad aprobada; registro real pendiente |
| CHAT-01 | Dos usuarios en dos dispositivos; A envía | B recibe nombre, vivienda, contenido y fecha | Verificado entre teléfono y emulador |
| SEC-01 | Vecino escribe directamente un anuncio con SDK/REST | Reglas rechazan | Pendiente |
| SEC-02 | Vecino intenta escribir rol o leer otra comunidad | Reglas rechazan | Pendiente |
| FCM-01 | Admin publica; B recibe y toca aviso | Abrir el mismo aviso por ID, incluso si ya no está entre los últimos 30 | Prueba local del emisor aprobada; despliegue y prueba real pendientes |
| MQTT-01 | Dos clientes; A confirma alarma | B recibe identidad y vivienda | Recepción verificada entre dos dispositivos |
| MQTT-02 | Repetir mismo UUID | Un solo aviso por ID | Unidad inicial implementada; integración pendiente |
| MQTT-03 | Desactivar red antes de publicar | Estado sin enviar | Pendiente |
| MQTT-04 | Cortar/restaurar red y publicar otro evento | Recuperar suscripción sin duplicación | Pendiente |
| MQTT-05 | Cliente de otra comunidad intenta tópico ajeno | Broker rechaza | Pendiente |
| MQTT-06 | Cliente se suscribe después de alarma pasada | No aviso antiguo como nuevo | Pendiente |
| LIFE-01 | App abierta/segundo plano/cerrada | Registrar recepción y límites en cada condición | Pendiente |
| PERM-01 | Denegar permiso notificación | No crash; explicar limitación | Pendiente |
| ADMIN-01 | Admin atiende/cierra; vecino lo intenta | Solo admin puede cambiar estado | Pendiente |
| UX-01 | Tres personas ejecutan tareas del documento | Registrar dificultades y cambios | Pendiente |
| PERF-01 | Entorno autorizado; carga definida y Profiler | CPU, memoria y red registrados | Pendiente |
