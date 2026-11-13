# Design: web de presentación (`docs/`)

> Alcance: la landing estática de GitHub Pages (`docs/index.html` + `docs/assets/site.css`) y, desde la sección «App Android: la cartera», la app Compose, que hereda el mismo mundo.

## Dirección: «La cartera y el ticket»
La web es una cartera que se abre al hacer scroll. Cada función de la app sale del bolsillo como una tarjeta de color con su móvil real completo, se queda apilada asomando por arriba, y al final la cartera se guarda y «paga»: un ticket de caja se imprime con el scroll y el total es **GRATIS**. Todo gira en torno al gasto de la compra. El enfoque sigue siendo **«toda tu compra en un sitio»** y el reconocimiento de productos es la última tarjeta.
Se rechaza la rejilla de tarjetas iguales con icono, el kicker sobre el titular, los brillos, los degradados de texto y el secuestro del scroll (nada de scroll suave por JS).

## Color
Estrategia *Full palette*: seis tarjetas con color propio sobre un suelo neutro teñido de verde.

| Tarjeta | Fondo | Texto |
|---|---|---|
| Tus supermercados | `#145A29` verde | blanco |
| Tarjeta de cliente | `#1E3A8A` azul tinta | blanco |
| La lista | `#A93C1A` terracota | blanco |
| Gastos | `#FFC83D` ámbar | `#1B1405` |
| Letras grandes | blanca (`#1D2923` en oscuro) | tinta |
| Reconoce productos | `#16221B` tinta | blanco |

Suelo `#E8EDE9` (oscuro `#0C110E`), tinta `#13201A` / secundaria `#3B4F43`, sección de instalar y pie `#0B2F18`. Ámbar para foco y números de paso. Todo el texto cumple ≥ 4,5:1. Franjas verde `#00843D` · blanco · negro `#111` solo en el pie (guiño a la bandera de Extremadura).

## Tipografía
- **Bricolage Grotesque** (OFL 1.1, de Mathieu Triay), alojada en `docs/fonts/` junto a su `OFL.txt`: sin peticiones a terceros. Titulares en peso 800 y `font-stretch: 88%`, `text-wrap: balance`. Titular principal hasta 5,6 rem.
- Cuerpo en la tipografía del sistema, **19 px / 1,6** (legibilidad ante todo para baja visión), medida ≤ ~70 caracteres.

## Espacio y forma
Tarjetas: radio 36 px (28 en móvil), sombra neutra con desplazamiento, 500 px de alto mínimo, el móvil sale por abajo con el borde inferior recortado. Secciones con 104 px de relleno (64 en móvil) y 20 px laterales. Botón principal 60 px de alto, radio 18.

## Componentes
- **Cartera de la cabecera** (SVG): piel verde `#0B2F18` con costura discontinua y cierre ámbar; cuatro tarjetas de color asoman en abanico. En la sección de funciones el bolsillo es el mismo, a todo el ancho.
- **Tarjeta (`.pass`)**: texto a un lado, móvil real **completo** al otro (bisel `#0F1512`, esquinas de 34 px), alternando lados.
- **Ticket (`.ticket`)**: papel `#FBFAF4` con borde inferior dentado (máscara cónica), tipografía monoespaciada tabular, una línea por función **sin precios** y total «GRATIS». Es decorativo (`aria-hidden`); el mensaje y el botón de descarga están en texto normal a su lado.
- **Pasos**: lista con número circular ámbar. **Tabla de privacidad**: tabla real con `<caption>` y `th scope`; apilada con etiquetas `data-l` en móvil. **Preguntas**: `<details>` con `+` / `–`.

## Movimiento
Librería: **GSAP 3.15 + ScrollTrigger**, alojadas en `docs/vendor/gsap/` (sin terceros; licencia Standard No Charge, cabecera intacta). Scroll siempre nativo.
- **Escritorio** (≥ 900×640, sin «reducir movimiento»): la cartera se fija (`pin`, `scrub`); cada tarjeta sube desde detrás del bolsillo, las anteriores quedan apiladas asomando 16 px y a escala −2,5 %; contador «n / 6» arriba; al final las tarjetas se guardan. Después el ticket se fija y se imprime desde una ranura (`yPercent` −100 → 0), con el sello GRATIS.
- **Móvil / pantallas bajas**: variante ligera sin fijar nada; cada tarjeta entra con un pequeño giro y el móvil hace parallax; el ticket se imprime al entrar en pantalla.
- **Sin JavaScript o con `prefers-reduced-motion`**: tarjetas apiladas en flujo normal (en escritorio sin JS, `position: sticky`), ticket completo y visible.

## Superficies del navegador
`::selection` ámbar, `caret-color` / `accent-color` / `scrollbar-color` verdes, subrayado con `text-underline-offset`, `color-scheme: light dark`, `theme-color` por esquema, cifras tabulares en la huella SHA‑256.

## Accesibilidad
Enlace «Saltar al contenido», un `h1`, títulos en orden, `alt` descriptivo en cada captura, objetivos táctiles ≥ 48 px, foco visible. Todo el contenido está en el DOM en orden de lectura; el JavaScript solo añade fijado y transformaciones. Las tarjetas no contienen elementos enfocables, así que el fijado no puede ocultar el foco. Funcional sin JS, con «reducir movimiento», en modo oscuro y a 200 % de zoom.

## Procedencia de las imágenes
Las capturas de `docs/img/` (WebP a 360 y 720 px, `srcset`) son **capturas reales a 1080×2424** del emulador Pixel_10 con una compilación *debug* del rediseño de la app y datos de demostración extremeños, con barra de estado limpia (modo demo de SystemUI: 9:41, batería llena). Cada WebP lleva su origen en el XMP. `app-salmon` y `app-pollo` proceden de la v1.10 (pantalla de reconocimiento aún no recapturada). No hay imágenes generadas.
