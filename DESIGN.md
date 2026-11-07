# Design: web de presentación (`docs/`)

> Alcance: la landing estática de GitHub Pages (`docs/index.html` + `docs/assets/site.css`). La interfaz de la app Android tiene su propio tema en Compose y no se rige por este documento.

## Dirección
La opción **clásica de la categoría, ejecutada a fondo** (nivel de oficio de referencia: Bring!). Una promesa, un móvil real, un botón. La prueba es la interfaz verdadera de la app, no ilustraciones. Sin kickers sobre el titular, sin tarjetas de icono + título, sin degradados de texto ni brillos.

## Color
Estrategia *Committed*: un verde de marca ocupa la cabecera completa; el resto es un suelo neutro teñido de verde.

| Rol | Claro | Oscuro |
|---|---|---|
| `--field` (cabecera) | `#145A29` | `#0F4A21` |
| `--field-deep` (instalar, pie) | `#0B2F18` | `#071A0E` |
| `--ground` | `#F4F7F3` | `#0D1510` |
| `--ink` | `#13241A` | `#E9F1EA` |
| `--ink-2` (secundario, teñido) | `#3C5143` | `#B5C7B9` |
| `--line` | `#D5DED6` | `#2A3A2E` |
| `--accent` (foco y números de paso) | `#FFC83D` | igual |

Todo el texto cumple ≥ 4,5:1 (el cuerpo supera 7:1). Tinte siempre desde el verde, nunca gris neutro. Franjas verde `#00843D` · blanco · negro `#111` solo en el pie (guiño a la bandera de Extremadura).

## Tipografía
Pila del sistema (`system-ui`): sin fuentes externas ni peticiones de terceros, coherente con la política de privacidad.
- Cuerpo **19 px / 1,6**, medida máx. ~36–46 em (≤ 70 caracteres).
- Display: peso 800, interlineado 1,08, tracking −0,02 em, `text-wrap: balance`. H1 `clamp(2.5rem, 1.4rem + 3.6vw, 4.4rem)` (≤ 6 rem); H2 hasta 3,1 rem; H3 hasta 1,75 rem.
- `text-wrap: pretty` en párrafos y listas. Cifras tabulares en la huella SHA‑256.

## Espacio y forma
Secciones con 104 px de relleno vertical (64 px en móvil) y 20 px laterales como *padding*. Contenedor 1160 px (820 px en preguntas). Radios: botón 16, panel 22, móvil 40/32. Sombras siempre con desplazamiento y desenfoque, neutras (`rgba(0,0,0,…)`), nunca halos de color.

## Componentes
- **Botón principal**: blanco sobre verde, 60 px de alto, peso 800; en móvil, ancho completo. Foco: anillo ámbar de 3 px.
- **Móvil**: bisel `#101512` liso con captura real; se recorta la barra de estado con `object-fit: cover; object-position: bottom`.
- **Pasos**: lista ordenada con número circular (verde en claro, ámbar en la sección oscura); el texto es un bloque, no una cuadrícula.
- **Filas de función**: texto + captura, alternando lados; separadas por una línea, sin tarjetas.
- **Tabla de privacidad**: tabla real con `<caption>` y `th scope`; en móvil se apila con etiquetas `data-l`.
- **Preguntas**: `<details>` con `+` / `–` como indicador.

## Movimiento
Un único momento: el móvil de la cabecera sube 28 px al cargar (`cubic-bezier(.16,1,.3,1)`, `transform` solo, contenido visible por defecto). `prefers-reduced-motion` lo desactiva todo.

## Superficies del navegador
`::selection` ámbar, `caret-color` y `accent-color` verdes, `scrollbar-color`, subrayado con `text-underline-offset`, `color-scheme: light dark` y `theme-color` por esquema.

## Accesibilidad
Enlace «Saltar al contenido», un solo `h1`, jerarquía de títulos sin saltos, `alt` descriptivo en cada captura, objetivos táctiles ≥ 48 px, foco visible, sin JavaScript.

## Procedencia de las imágenes
Las 7 capturas de `docs/img/` son **capturas reales** de la app en un emulador Pixel_10 con datos de demostración; cada PNG lleva su origen incrustado (`impeccable embed-prompt`). No hay imágenes generadas.
