# Truco Argentino — TP Java

Juego de Truco argentino para **2 jugadores** (sin bots), hecho en **Java + Maven**,
con una **interfaz gráfica (Swing)** para jugar en pantalla y también una
**versión por consola** como alternativa.

---

## 1. Cómo compilar y ejecutar

Requiere **Java 17+** y **Maven** instalados.

```bash
# Compilar
mvn clean package

# Ejecutar la interfaz gráfica (recomendado para mostrarlo en clase)
mvn exec:java

# o directamente con el .jar generado
java -jar target/truco-argentino.jar

# Ejecutar la versión por consola/terminal en vez de la interfaz gráfica
java -jar target/truco-argentino.jar consola
```

Al abrir la interfaz gráfica van a ver:
1. **Pantalla de inicio**: se cargan los nombres de los 2 jugadores y si se juega a 15 o 30 puntos.
2. **Pantalla de "pase de turno"**: como es un juego de un solo dispositivo (hotseat),
   antes de mostrar la mano de cada jugador aparece un cartel de "pasá el dispositivo",
   así el rival no ve las cartas ajenas.
3. **Pantalla de juego**: la mesa con las cartas jugadas, el puntaje, el nivel de Truco
   vigente, y los botones de acción (jugar carta, cantar Truco, Envido, Flor, Quiero/No quiero, irse al mazo).
4. **Pantalla final**: anuncia el ganador de la partida y permite jugar de nuevo.

---

## 2. Reglas implementadas (según el reglamento tradicional)

### Jerarquía de las cartas
Se usa la baraja española de 40 cartas (4 palos x 1-7,10,11,12). De mayor a menor:

| Jerarquía | Carta(s) |
|---|---|
| 1° | 1 de Espada (Ancho de espada) |
| 2° | 1 de Basto (Ancho de basto) |
| 3° | 7 de Espada |
| 4° | 7 de Oro |
| 5° | Los cuatro 3 |
| 6° | Los cuatro 2 |
| 7° | 1 de Oro, 1 de Copa, 12, 11 y 10 de cualquier palo |
| 8° | 7 de Basto y 7 de Copa |
| 9° | Los cuatro 6 |
| 10° | Los cuatro 5 |
| 11° (última) | Los cuatro 4 |

Esto está codificado en `Carta.jerarquiaTruco()`.

### Manos y rondas
- Se reparten 3 cartas a cada jugador por ronda.
- Se juegan hasta 3 "manos" (bazas): en cada una, cada jugador tira una carta y gana
  la de mayor jerarquía. Si empatan, es "parda".
- Gana la ronda quien se lleva 2 de las 3 manos, aplicando las reglas clásicas de parda:
  - Si la 1ª mano es parda, gana la ronda quien gane la 2ª.
  - Si la 1ª y 2ª son pardas, decide la 3ª; si también es parda, gana el jugador "mano".
  - Si hay un ganador en la 1ª y la 2ª es parda, gana la ronda quien ganó la 1ª.
  - Si ganan una mano cada uno, decide la 3ª; si es parda, gana quien ganó la 1ª.

Esta lógica está en `Ronda.evaluarGanadorRonda()`.

### Sistema de Truco / Quiero-No quiero
- Cualquiera de los dos jugadores puede cantar **Truco** (2 puntos), y luego escalar a
  **Retruco** (3 puntos) y **Vale cuatro** (4 puntos).
- El rival puede responder **Quiero** (se sigue jugando por esos puntos), **No quiero**
  (la ronda termina ahí y el que cantó se lleva los puntos del nivel anterior), o
  **Quiero y subo** (acepta y escala al siguiente nivel).
- Si nadie cantó Truco, la ronda vale 1 punto.
- Un jugador también se puede **ir al mazo** en su turno, perdiendo la ronda y cediendo
  al rival los puntos que estén en juego en ese momento.

### Puntaje de la partida
- Se juega a **15 o 30 puntos** (a elección, "malas" y "buenas" del Truco real).
- Gana la partida el primer jugador en alcanzar el puntaje objetivo.

### Envido
- Los **tantos** se calculan con las 3 cartas repartidas al comienzo de la ronda (aunque ya se
  hayan jugado): dos cartas del mismo palo valen **20 + la suma de sus valores**; las figuras
  (10, 11 y 12) valen **0**. Si no hay dos cartas del mismo palo, los tantos son el valor de la
  carta más alta. Ej: 7 y 6 de Espada = 33; Rey y 5 de Copa = 25.
- Cantos: **Envido** (2), **Real Envido** (3) y **Falta Envido** (lo que le falta al que va
  ganando para llegar a los 15/30 puntos). Se pueden encadenar subiendo la apuesta
  (Envido, Envido, Real Envido, Falta Envido) y los puntos se suman; la Falta Envido reemplaza al resto.
- Se puede cantar **una sola vez por ronda**, solo en la **primera mano** y **antes de aceptar el Truco**.
  "El envido está primero": el que recibe un Truco puede contestar con Envido (o Flor) antes de
  responder el Truco.
- **Quiero**: gana el que tiene más tantos y, si empatan, gana el jugador **mano**. Se muestran los
  tantos de los dos. **No quiero**: el que cantó suma lo que ya estaba aceptado (1 punto si fue un solo canto).
- Si con el envido alguien llega al puntaje de la partida, la ronda termina en ese momento.

### Flor
- Hay flor cuando las **3 cartas repartidas son del mismo palo**. Sus tantos son 20 + la suma de
  las tres (figuras = 0). Solo se muestra el botón **FLOR** al jugador que la tiene.
- Se canta en la primera mano, antes de aceptar el Truco, y no hay "quiero / no quiero":
  si solo uno la tiene, suma **3 puntos**; si **los dos** tienen flor, gana la más alta
  (si empatan, el mano) y se lleva **6 puntos**.
- **La flor anula el envido**: si alguien canta Envido y cualquiera de los dos tiene flor, el envido
  no se juega y se resuelve la flor.
- La cantidad de puntos de la flor se cambia en una sola constante: `Ronda.PUNTOS_FLOR`.

### No incluido (a propósito)
Contraflor, Contraflor al resto y "con flor me achico". Tampoco se puede cantar envido
una vez que el Truco ya fue aceptado.

---

## 3. Organización del código (clases y objetos)

```
com.truco
 ├── Main.java                  → punto de entrada (GUI o consola)
 ├── modelo/
 │   ├── Palo.java               → enum de los 4 palos
 │   ├── Carta.java              → un naipe + su jerarquía de Truco
 │   ├── Mazo.java                → las 40 cartas, mezclar y repartir (usa Deque)
 │   └── Jugador.java             → nombre, mano (List<Carta>), puntos, bazas ganadas,
 │                                   tantos de envido y flor (sobre las cartas repartidas)
 ├── logica/
 │   ├── NivelTruco.java          → enum Truco/Retruco/Vale cuatro y sus puntos
 │   ├── TipoEnvido.java          → enum Envido/Real Envido/Falta Envido y sus puntos
 │   ├── Ronda.java               → reparte cartas, juega las 3 manos, resuelve pardas,
 │   │                               maneja el canto de Truco y Quiero/No quiero,
 │   │                               y los tantos (Envido, Flor)
 │   └── Partida.java             → controla el puntaje y crea rondas hasta el objetivo
 ├── excepciones/
 │   └── JugadaInvalidaException.java  → jugadas o entradas inválidas
 ├── gui/                        → interfaz gráfica Swing
 │   ├── TrucoApp.java            → ventana principal (CardLayout entre pantallas)
 │   ├── PanelInicio.java
 │   ├── PanelTurno.java
 │   ├── PanelJuego.java
 │   └── PanelFinPartida.java
 └── consola/
     └── JuegoConsola.java        → versión 100% texto por terminal
```

### Requisitos técnicos cumplidos
- **Java + Maven**: `pom.xml` con `exec-maven-plugin` y `maven-jar-plugin`.
- **Clases y objetos**: `Carta`, `Mazo`, `Jugador`, `Ronda`, `Partida`, etc.
- **Colecciones**: `List<Carta>` (mano de cada jugador), `Deque<Carta>` (mazo),
  `List<Jugador>`/`List<Carta[]>` (resultados y jugadas de cada mano).
- **Excepciones**: `JugadaInvalidaException` (unchecked) se lanza y se captura en la
  GUI y en la consola para validar jugadas fuera de turno, índices de carta
  inválidos, cantos duplicados, respuestas sin canto pendiente, puntos negativos, etc.

---

## 4. Posibles preguntas del profesor (guía rápida)

- **¿Por qué Swing y no solo consola?** Porque se permite mostrarlo en pantalla, y una
  interfaz gráfica hace mucho más clara la mesa, el turno y el estado del Truco que
  scrollear texto en la terminal. La versión consola se mantiene como alternativa
  (`java -jar truco-argentino.jar consola`) usando exactamente la misma lógica del juego
  (`Ronda` y `Partida`), lo que demuestra que la lógica está desacoplada de la interfaz.
- **¿Cómo se resuelve un empate ("parda")?** Ver sección "Manos y rondas" arriba y el
  método `Ronda.evaluarGanadorRonda()`.
- **¿Cómo se calcula el envido si ya se jugó una carta?** Con las 3 cartas repartidas al inicio:
  `Jugador` guarda esa mano inicial aparte de la mano que va quedando (`Jugador.tantosEnvido()`).
- **¿Qué pasa si alguien canta envido y el rival tiene flor?** La flor anula el envido
  (`Ronda.cantarEnvido()` llama a `resolverFlor()`), y se lleva los puntos de la flor.
- **¿Qué pasa si el mazo se queda sin cartas?** `Partida.iniciarNuevaRonda()` rearma y
  mezcla un mazo nuevo de 40 cartas cuando quedan menos de 6 disponibles.
