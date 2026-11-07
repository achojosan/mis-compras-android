# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

(La superficie de esta tarea es la **web de presentación y descarga** en `docs/`, HTML/CSS estático servido con GitHub Pages: ahí el modo es web. El producto en sí es una app Android.)

## Users
Cualquier persona que hace la compra del supermercado y quiere una lista más cómoda, **con especial atención a quien tiene dificultades visuales o cognitivas** y a mayores: gente que no puede fiarse de los ojos para distinguir un producto de otro. La página la abre alguien en el móvil (a menudo al buscar «lista de la compra» o al recibir el enlace de un familiar) y debe decidir si instalar un APK fuera de Google Play.

## Product Purpose
Mis Compras es una lista de la compra para Android que **reconoce los productos con la cámara**: apuntas al artículo, el móvil vibra y una pantalla con letras enormes dice qué es. Nació para distinguir envases casi idénticos (p. ej. latas de comida para gatos con el mismo diseño donde solo cambia la pegatina del sabor). Éxito: que una persona la instale con confianza y que le sirva en el supermercado.

## Positioning
Distingue productos de aspecto casi idéntico combinando código de barras y **reconocimiento visual en el propio móvil con fotos que registra el usuario**, sin internet, sin cuentas y sin anuncios. Además: tiendas cercanas, tarjeta de cliente (códigos de barras y QR) en pantalla completa, lista por tienda, gastos, texto «muy grande» sin deformarse.

## Operating Context
Se usa de pie en el supermercado, con una mano, a veces con poca luz, el móvil a medio brazo, con el texto del sistema agrandado. Se distribuye como **APK desde GitHub Releases** (no está en Google Play): el visitante debe permitir instalar de una fuente desconocida y puede ver el aviso de Google Play Protect.

## Capabilities and Constraints
- Android 12 o superior; APK de unos 78 MB (solo arm64); se actualiza desde la propia app (una consulta diaria a GitHub, desactivable).
- **Gratuita**, sin cuentas, sin anuncios, sin compras dentro de la app (confirmado por el autor).
- Solo en español. Funciona sin conexión salvo tres consultas opcionales: tiendas cercanas (coordenadas aproximadas a OpenStreetMap), autocompletar un código (Open Food Facts) y avisar de versiones (GitHub).
- Sin versión para iPhone por ahora. Sin licencia elegida (todos los derechos reservados).
- Limitación honesta: la precisión del reconocimiento con latas casi idénticas aún no está calibrada con productos reales; algunas cadenas renuevan el QR de su app, y una copia no valdría.

## Brand Commitments
- Nombre: **Mis Compras**. Idioma: español (es-ES).
- **«Hecha en Extremadura»** debe constar (petición expresa del autor); autor: Josan (@achojosan), contacto achojosan@proton.me.
- Conservar el contenido de la **guía de instalación** (incluida la nota de Play Protect y la huella SHA-256 de la firma) y de **privacidad** (qué sale del móvil).
- **Landing, segunda ronda (confirmada):** el enfoque de la web pasa a ser **«toda tu compra en un sitio»** (tiendas cercanas con horario, tarjeta de cliente, lista por tienda, gastos, accesibilidad); el reconocimiento de productos es **una función más**, no el protagonista. Se explora **otra dirección visual**; el estilo clásico anterior ya no es vinculante.
- No hay otro compromiso visual vinculante: el diseño anterior se reemplaza por completo.

## Evidence on Hand
- Capturas **reales** de la app en `docs/screenshots/` con datos de demostración (productos extremeños: Torta del Casar, Queso de la Serena, Cerezas del Jerte…); deben etiquetarse como demostración.
- Releases publicadas en GitHub (APK firmado; huella SHA-256 de la firma `f78c9ebe…b4d5`), 50 tests unitarios, prueba real de actualización 1.8 → 1.9 en emulador.
- **Ausencias que no se deben inventar:** opiniones o testimonios, número de usuarios o descargas, valoraciones, prensa, presencia en Google Play, capturas en móviles reales de terceros.

## Product Principles
1. **Claridad antes que adorno:** una persona con baja visión debe poder leer y usar la página tan bien como la app.
2. **Honestidad:** decir qué sale del móvil, que el APK va fuera de Play y que el reconocimiento aún se afina.
3. **Mostrar el producto real** (capturas verdaderas) en lugar de ilustrar promesas.
4. **Instalar sin miedo:** el camino de descarga, permisos y avisos del sistema se explica paso a paso y se puede verificar.

## Accessibility & Inclusion
La propia página ha de cumplir como mínimo WCAG 2.2 AA: contraste, texto base grande, zoom hasta 200 % sin romperse, foco visible, respeto de `prefers-reduced-motion` y de los modos claro/oscuro, objetivos táctiles amplios y lenguaje sencillo.
