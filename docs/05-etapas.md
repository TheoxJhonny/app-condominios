# Etapas de implementación

| Etapa | Entrega | Criterio de cierre |
|---|---|---|
| 0 | Requisitos, diseño técnico, README, docs y Git | Revisar Excel y cerrar dudas de alcance |
| 1 | Base Compose y dominio | Build Android, tests y navegación en dispositivo |
| 2 | Firebase Auth, perfil y pertenencia | Registro/login/recuperación reales; no autoasignación de rol |
| 3 | Chat y anuncios con RTDB | Dos dispositivos y pruebas de reglas |
| 4 | FCM y apertura de anuncio | Publicación real genera notificación y navegación segura |
| 5 | MQTT conectado a UI e historial | Dos clientes, QoS 1, TLS, ACL, deduplicación y reintento |
| 6 | Funciones adicionales y diseño completo | Cerrar compraventa según rúbrica; mejora propia demostrable |
| 7 | Validación integral y entrega técnica | Usabilidad con 3 personas, rendimiento, resiliencia, evidencia y manual |

Hacer commits por entregas verificables; registrar fallos y pendientes. No marcar etapas completadas por tener únicamente clases o pantallas vacías.

## Dirección visual

Diseño propio: superficies claras, verde profundo y acentos discretos, tipografía con jerarquía, tarjetas espaciosas y navegación estable. Alertas con color semántico y texto explícito. Diseñar estados vacíos, de error y envío antes del pulido final. Evitar que el botón de emergencia compita con navegación normal; confirmación accesible en lugar de depender exclusivamente de una pulsación prolongada.

La UI actual es una base técnica provisional. Faltan iconos adecuados, semántica de accesibilidad, pantallas completas, ajuste al teclado, tamaños de texto, contraste y pruebas en distintas dimensiones. No se basa en los diseños de los documentos.
