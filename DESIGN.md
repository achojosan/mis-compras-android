# Design: web de presentación (`docs/`)

> Alcance: la landing estática de GitHub Pages (`docs/index.html` + `docs/assets/site.css`). La interfaz de la app Android tiene su propio tema en Compose y no se rige por este documento.

## Dirección: «Cartera de tarjetas»
La web es un monedero. Cada función de la app es una tarjeta de color con esquinas muy redondeadas que se apila sobre la anterior al desplazarse; el móvil real de cada función asoma por su borde inferior. El enfoque es **«toda tu compra en un sitio»** (tiendas, tarjeta de cliente, lista, gastos, letras grandes) y el reconocimiento de productos es una tarjeta más, la última.
Se rechaza la rejilla de tarjetas iguales con icono, el kicker sobre el titular, los brillos y los degradados de texto.

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
- **Abanico de la cabecera**: tres miniaturas de tarjeta (3:2) con recortes reales de la interfaz (tiendas, tarjeta de cliente, lista), inclinadas y superpuestas.
- **Tarjeta (`.pass`)**: texto a un lado, móvil real al otro, alternando lados; en el modo escritorio con altura suficiente se apila con `position: sticky` (14 px de asomo por tarjeta), sin JavaScript.
- **Móvil**: bisel `#101512` liso; se recorta la barra de estado con margen negativo proporcional.
- **Pasos**: lista con número circular ámbar. **Tabla de privacidad**: tabla real con `<caption>` y `th scope`; apilada con etiquetas `data-l` en móvil. **Preguntas**: `<details>` con `+` / `–`.

## Movimiento
Un único momento: al cargar, las tres miniaturas «se reparten» (suben 30 px). Más el apilado sticky, que es CSS puro. `prefers-reduced-motion` lo desactiva todo.

## Superficies del navegador
`::selection` ámbar, `caret-color` / `accent-color` / `scrollbar-color` verdes, subrayado con `text-underline-offset`, `color-scheme: light dark`, `theme-color` por esquema, cifras tabulares en la huella SHA‑256.

## Accesibilidad
Enlace «Saltar al contenido», un `h1`, títulos en orden, `alt` descriptivo en cada captura, objetivos táctiles ≥ 48 px, foco visible, sin JavaScript y funcional en modo oscuro y a 200 % de zoom.

## Procedencia de las imágenes
Las 8 capturas de `docs/img/` son **capturas reales** de la app en un emulador Pixel_10 con datos de demostración; cada PNG lleva su origen incrustado. No hay imágenes generadas.
