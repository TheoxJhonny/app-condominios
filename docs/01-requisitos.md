# Revisión documental y alcance

Fecha: 21 de noviembre de 2026. Proyecto: App Condominios / Comunidad Conectada.

## Documentos

- **Programacion en Android - EV5 - Firebase MQTT.docx**: texto completo leído, 16 páginas. Contiene inconsistencias de nombres: portada Evaluación 4, nombre de archivo EV5, referencia a Java y errores tipográficos FCM/FMC. Se usan los requisitos funcionales como referencia.
- **Informe técnico.docx**: leído completo, 3 páginas. Describe estructura del informe, defensa y entregables. El equipo de implementación aportará documentación y evidencia; no redactará el informe.
- **Programacion en Android - EV5 - Firebase MQTT Rubrica.xlsx**: identificado, pero no leído. La extracción no devuelve contenido y la descarga falló por DNS. No afirmar revisión completa ni puntajes hasta abrirlo.

## Decisiones confirmadas por Jonathan

- Kotlin + Jetpack Compose autorizados, aunque el documento diga Java.
- Diseño original y moderno. No replicar las ilustraciones ni el diseño de los documentos.
- Prioridad: aplicación, Firebase y MQTT; informe a cargo de otra persona.
- Mantener README para GitHub y documentación Markdown por etapas en `docs/`.
- No existen aún proyecto Firebase ni repositorio GitHub.

## Matriz inicial

| Área | Requisito | Estado |
|---|---|---|
| Cuenta | Registro, login, cierre y recuperación | Adaptador preparado; UI e integración pendientes |
| Perfil | Nombre editable, vivienda alfanumérica, UID único | Modelo y validación inicial |
| Permisos | Vecino/administrador; rol no elegible | Diseño con claims; reglas propuestas sin probar |
| Chat | Nombre, vivienda, contenido, fecha; tiempo real e historial limitado | Adaptador con últimos 50; UI pendiente |
| Anuncios | Solo admin publica; todos leen | Escritura preparada; lectura/UI pendientes |
| FCM | Aviso al publicar y apertura del anuncio | Pendiente, requiere emisor confiable |
| MQTT | Confirmación, UUID, identidad, comunidad, fecha y estado | Transporte preparado; orquestación/UI pendientes |
| MQTT | TLS, autenticación, ACL, QoS 1, sin retain, reconexión | Cliente preparado; broker no configurado |
| Alertas | Historial y cierre/atención por admin | Adaptador preparado; pruebas pendientes |
| Seguridad | Datos solo para miembros autenticados | Reglas propuestas; probar en Emulator Suite |
| Calidad | Carga, error, reintento, listeners y permisos | Implementación pendiente en UI |
| Creatividad | Al menos una mejora propia | Propuesta: estado de conexión/envío y prevención de activación repetida |
| Compraventa | Mencionada en descripción, creatividad y pruebas | Alcance exacto pendiente de leer Excel; no excluir |
| Google | Inicio de sesión opcional con bonificación | Diferido |
| Pruebas | Dos dispositivos, seguridad, resiliencia, UX y rendimiento | Ver matriz de pruebas |

La identidad es el UID de Firebase Auth, nunca el número de vivienda. Se permiten varios residentes por vivienda. El lanzamiento inicial puede trabajar con una comunidad, conservando separación de rutas y tópicos por comunidad.
