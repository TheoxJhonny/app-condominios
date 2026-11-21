# Pruebas y evidencia

No presentar un test escrito como si estuviera ejecutado. No incluir capturas simuladas como evidencia.

## Casos de aceptación pendientes

| ID | Condiciones y pasos | Esperado | Estado |
|---|---|---|---|
| REG-01 | Registro con todos los campos vacíos | Errores; no crear cuenta | Unidad implementada; UI pendiente |
| REG-02 | Registro con vivienda 311A | Aceptar; conservar vivienda | Unidad implementada; Firebase pendiente |
| CHAT-01 | Dos usuarios en dos dispositivos; A envía | B recibe nombre, vivienda, contenido y fecha | Pendiente |
| SEC-01 | Vecino escribe directamente un anuncio con SDK/REST | Reglas rechazan | Pendiente |
| SEC-02 | Vecino intenta escribir rol o leer otra comunidad | Reglas rechazan | Pendiente |
| FCM-01 | Admin publica; B recibe y toca aviso | Abrir anuncio correcto | Pendiente |
| MQTT-01 | Dos clientes; A confirma alarma | B recibe identidad y vivienda | Pendiente |
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

Por cada ejecución guardar fecha, dispositivos/API, versión/commit, condiciones, pasos, esperado, obtenido, evidencia y estado. Las pruebas de carga deben definir clientes, mensajes y duración y ejecutarse solo en sistemas propios/autorizados.

## Automatización

`CommunityTest` contiene siete casos de dominio. Ejecutar con `./gradlew.bat testDebugUnitTest`. Las pruebas unitarias no prueban Firebase, broker, notificaciones ni permisos.

## Restricciones encontradas el 21-11-2026

- Inicio de Gradle con caché por defecto: fallo al cargar biblioteca nativa en directorio no escribible.
- Segundo intento con caché dentro del workspace: Gradle 9.3.1 inicia, pero la conexión local al daemon es denegada (`Permission denied: getsockopt`). No se llegó a compilar Android.
- Descarga de rúbrica Excel: fallo DNS. Extracción textual e intento por páginas no producen contenido.
- Por tanto, compilación Android y pruebas contra servicios reales siguen pendientes.
