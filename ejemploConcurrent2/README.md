# Ejemplos de java.util.concurrent

Colección de ejemplos progresivos para enseñar la API moderna de concurrencia en Java.

## Contenido

### Teoría
- **[TEORIA_CONCURRENT.md](TEORIA_CONCURRENT.md)** - Documento completo con toda la teoría, analogías y explicaciones detalladas

### Ejemplos Progresivos

1. **[Ejemplo01_ExecutorBasico.java](src/Ejemplo01_ExecutorBasico.java)**
   - Comparación: `new Thread()` vs `ExecutorService`
   - Concepto de reutilización de hilos
   - Pool básico con `FixedThreadPool`

2. **[Ejemplo02_TiposDePoolsDemo.java](src/Ejemplo02_TiposDePoolsDemo.java)**
   - `FixedThreadPool` (número fijo de hilos)
   - `CachedThreadPool` (hilos bajo demanda)
   - `SingleThreadExecutor` (ejecución secuencial)
   - Cuándo usar cada uno

3. **[Ejemplo03_CallableYFuture.java](src/Ejemplo03_CallableYFuture.java)**
   - Limitaciones de `Runnable`
   - `Callable<T>` para devolver resultados
   - `Future<T>` como promesa de valor futuro
   - Gestión de excepciones con `ExecutionException`
   - `isDone()` para comprobar estado

4. **[Ejemplo04_FutureConTimeout.java](src/Ejemplo04_FutureConTimeout.java)**
   - `future.get(timeout, TimeUnit)` para evitar bloqueos
   - Cancelación de tareas con `cancel()`
   - Múltiples APIs en paralelo con timeout individual
   - Patrón de reintentos

5. **[Ejemplo05_EjemploReal_Descargador.java](src/Ejemplo05_EjemploReal_Descargador.java)**
   - Ejemplo realista: descargador de archivos paralelo
   - Pool de 3 hilos descargando 7 archivos
   - Manejo de errores y timeouts
   - Comparación de tiempos (secuencial vs paralelo)

### Ejercicios para Alumnos

- **[Ejercicio01_PLANTILLA.java](src/Ejercicio01_PLANTILLA.java)** - Conversor de temperaturas (con TODOs)

## Cómo ejecutar

```bash
# Compilar todos los ejemplos
javac src/*.java

# Ejecutar un ejemplo específico
java -cp src Ejemplo01_ExecutorBasico
java -cp src Ejemplo02_TiposDePoolsDemo
java -cp src Ejemplo03_CallableYFuture
java -cp src Ejemplo04_FutureConTimeout
java -cp src Ejemplo05_EjemploReal_Descargador
```

## Orden recomendado de estudio

1. Leer primero la sección 1 y 2 de **TEORIA_CONCURRENT.md**
2. Ejecutar **Ejemplo01** y **Ejemplo02**
3. Leer sección 3 y 4 de **TEORIA_CONCURRENT.md**
4. Ejecutar **Ejemplo03** y **Ejemplo04**
5. Ejecutar **Ejemplo05** (caso real)
6. Leer sección 6 (Ejercicios propuestos)
7. Intentar resolver **Ejercicio01_PLANTILLA.java**

## Conceptos clave

- **Thread Pool**: Conjunto de hilos reutilizables
- **ExecutorService**: Gestor de tareas asíncronas
- **Callable**: Tarea que devuelve resultado
- **Future**: Promesa de valor futuro
- **Timeout**: Límite de tiempo de espera

## Analogías pedagógicas

- 🏗️ **Constructor y ladrillos**: Pool vs new Thread
- 💇 **Peluquería**: FixedThreadPool
- 🍽️ **Restaurante**: CachedThreadPool
- 📮 **Correos**: SingleThreadExecutor
- 🎫 **Ticket de recogida**: Future

## Próximos temas (avanzado)

- `CompletableFuture` (Java 8+)
- `ForkJoinPool` y Streams paralelos
- `CountDownLatch`, `CyclicBarrier`
- `Semaphore` y `Lock`

---

**Autor**: Material didáctico para DAM - Programación de Procesos y Servicios
**Versión**: 1.0
**Fecha**: Diciembre 2025
