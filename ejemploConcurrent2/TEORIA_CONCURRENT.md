# API Moderna de Java: java.util.concurrent

## Índice
1. [Del pasado al presente: Por qué cambiar](#1-del-pasado-al-presente-por-qué-cambiar)
2. [Thread Pools y Executors](#2-thread-pools-y-executors)
3. [Callable vs Runnable](#3-callable-vs-runnable)
4. [Future: Resultados asíncronos](#4-future-resultados-asíncronos)
5. [Ejemplos completos](#5-ejemplos-completos)
6. [Ejercicios propuestos](#6-ejercicios-propuestos)

---

## 1. Del pasado al presente: Por qué cambiar

### 1.1 El problema con `new Thread()`

Hasta ahora habéis aprendido a crear hilos así:

```java
Thread hilo = new Thread(() -> {
    System.out.println("Hola desde el hilo");
});
hilo.start();
```

**Problemas de este enfoque:**

1. **Coste de creación**: Cada `new Thread()` es **muy costoso** en recursos:
   - Reserva memoria (por defecto 1MB de stack por hilo)
   - Realiza llamadas al sistema operativo
   - Crea estructuras de datos internas

2. **Sin límite de hilos**: Si tu aplicación recibe 10.000 peticiones, ¿creas 10.000 hilos?
   - Sobrecarga del sistema
   - Posible `OutOfMemoryError`
   - Context switching excesivo (cambio entre hilos)

3. **Sin reutilización**: Cada hilo se crea, ejecuta su tarea y muere. Después hay que crear otro.

4. **Gestión manual**: Tienes que gestionar tú mismo:
   - El ciclo de vida de los hilos
   - Excepciones no capturadas
   - Resultados de las tareas
   - Cancelación de tareas

### 1.2 La solución: Thread Pools

**Analogía del constructor:**

Imagina que eres un constructor y tienes que colocar 1.000 ladrillos:

- **Enfoque antiguo (`new Thread`)**: Contratas a un obrero nuevo para cada ladrillo. Le pagas, coloca un ladrillo, lo despides, y contratas al siguiente. ¿Absurdo, verdad?

- **Enfoque moderno (Thread Pool)**: Contratas una cuadrilla de 10 obreros al inicio del día. Les vas pasando ladrillos (tareas) de una pila. Cuando terminan uno, cogen el siguiente. Al final del día, despides a toda la cuadrilla.

**Ventajas del Thread Pool:**
- ✅ Los hilos se **reutilizan**
- ✅ Número **limitado y controlado** de hilos
- ✅ **Menor overhead** (no crear/destruir constantemente)
- ✅ **Cola de tareas**: Si todos los hilos están ocupados, las tareas esperan en una cola
- ✅ **Gestión automática** del ciclo de vida

---

## 2. Thread Pools y Executors

### 2.1 La interfaz ExecutorService

`ExecutorService` es la interfaz principal que representa un pool de hilos. Puedes pensar en él como un **gestor de trabajadores**.

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
```

**Operaciones principales:**

| Método | Descripción |
|--------|-------------|
| `execute(Runnable)` | Ejecuta una tarea sin devolver resultado |
| `submit(Runnable)` | Ejecuta una tarea y devuelve un `Future<?>` |
| `submit(Callable<T>)` | Ejecuta una tarea y devuelve un `Future<T>` con resultado |
| `shutdown()` | No acepta más tareas, pero termina las pendientes |
| `shutdownNow()` | Intenta cancelar todas las tareas (interrumpe hilos) |
| `awaitTermination(long, TimeUnit)` | Espera a que terminen todas las tareas |

### 2.2 Tipos de Thread Pools

Java proporciona varios tipos de pools a través de la clase `Executors`:

#### A) FixedThreadPool

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
```

**Características:**
- Número **fijo** de hilos (ej: 4 hilos)
- Si hay más tareas que hilos, las tareas esperan en una **cola ilimitada**
- Los hilos se reutilizan constantemente

**Cuándo usar:**
- Conoces la carga de trabajo
- Quieres limitar el uso de CPU/memoria
- Ejemplo: Servidor con 4 cores → 4 hilos

**Analogía:** Una peluquería con 4 peluqueros fijos. Si hay 10 clientes, 4 se atienden y 6 esperan sentados.

#### B) CachedThreadPool

```java
ExecutorService executor = Executors.newCachedThreadPool();
```

**Características:**
- Crea hilos **bajo demanda**
- Si un hilo está libre más de 60 segundos, se elimina
- **Sin límite** de hilos (¡peligro!)

**Cuándo usar:**
- Muchas tareas **cortas** y **asíncronas**
- Carga de trabajo impredecible
- Ejemplo: Servidor de chat con mensajes rápidos

**Analogía:** Un restaurante que contrata camareros según la cantidad de clientes, y los despide si no hay trabajo.

⚠️ **Peligro:** Puede crear miles de hilos y colapsar el sistema si hay muchas tareas largas.

#### C) SingleThreadExecutor

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
```

**Características:**
- **Solo 1 hilo**
- Garantiza que las tareas se ejecutan **secuencialmente** (una detrás de otra)
- Útil cuando necesitas orden estricto

**Cuándo usar:**
- Necesitas procesar tareas en orden
- Actualización de una base de datos (evitar condiciones de carrera)
- Ejemplo: Procesamiento de logs en orden

**Analogía:** Una ventanilla de correos con un solo empleado. Todo el mundo espera su turno.

#### D) ScheduledThreadPool

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
```

**Características:**
- Permite **programar tareas** en el futuro
- Tareas **periódicas** (cada X segundos)

**Cuándo usar:**
- Tareas programadas
- Ejemplo: Limpieza de caché cada 5 minutos

```java
// Ejecutar después de 5 segundos
executor.schedule(() -> System.out.println("Hola"), 5, TimeUnit.SECONDS);

// Ejecutar cada 10 segundos
executor.scheduleAtFixedRate(() -> System.out.println("Tick"), 0, 10, TimeUnit.SECONDS);
```

### 2.3 Ejemplo básico completo

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EjemploExecutor {
    public static void main(String[] args) {
        // Crear pool de 3 hilos
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Enviar 10 tareas
        for (int i = 1; i <= 10; i++) {
            int numeroTarea = i;
            executor.execute(() -> {
                String nombreHilo = Thread.currentThread().getName();
                System.out.println("Tarea " + numeroTarea + " ejecutada por " + nombreHilo);

                // Simular trabajo
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Apagar el executor
        executor.shutdown();

        System.out.println("Todas las tareas enviadas");
    }
}
```

**Salida esperada:**
```
Todas las tareas enviadas
Tarea 1 ejecutada por pool-1-thread-1
Tarea 2 ejecutada por pool-1-thread-2
Tarea 3 ejecutada por pool-1-thread-3
Tarea 4 ejecutada por pool-1-thread-1  ← Reutilización del hilo 1
Tarea 5 ejecutada por pool-1-thread-2
...
```

**Observaciones:**
- Solo 3 hilos ejecutan las 10 tareas (reutilización)
- "Todas las tareas enviadas" aparece inmediatamente (asíncrono)

### 2.4 Shutdown: Apagar correctamente el Executor

**Importante:** Siempre debes cerrar el `ExecutorService` o tu programa no terminará.

```java
// Opción 1: Shutdown suave
executor.shutdown(); // No acepta más tareas, pero termina las pendientes

// Opción 2: Shutdown forzado
List<Runnable> tareasNoEjecutadas = executor.shutdownNow(); // Interrumpe hilos

// Opción 3: Esperar a que termine
executor.shutdown();
try {
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // Forzar si tarda más de 60 segundos
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
}
```

**Patrón recomendado (try-with-resources - Java 19+):**
```java
try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
    // Usar el executor
    executor.submit(() -> System.out.println("Tarea"));
} // Se cierra automáticamente
```

---

## 3. Callable vs Runnable

### 3.1 Limitaciones de Runnable

Hasta ahora has usado `Runnable`:

```java
Runnable tarea = () -> {
    System.out.println("Hola");
    // No puedo devolver nada
    // No puedo lanzar excepciones chequeadas
};
```

**Problemas:**
1. ❌ El método `run()` devuelve `void` → No puedes obtener un resultado
2. ❌ No puede lanzar excepciones chequeadas (como `IOException`)

**Ejemplo del problema:**

```java
executor.execute(() -> {
    int resultado = calcularAlgoComplejo(); // Tarda 5 segundos
    // ¿Cómo devuelvo 'resultado' al hilo principal? ¡No puedo!
});
```

### 3.2 Callable: Tareas que devuelven resultados

`Callable<T>` es como `Runnable`, pero:
- ✅ Devuelve un valor de tipo `T`
- ✅ Puede lanzar excepciones chequeadas

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

**Comparación:**

| Característica | Runnable | Callable<T> |
|----------------|----------|-------------|
| Método | `void run()` | `T call()` |
| Devuelve valor | ❌ No | ✅ Sí (tipo T) |
| Lanza excepciones | Solo unchecked | ✅ Cualquiera (`throws Exception`) |
| Uso | `execute()` | `submit()` |

### 3.3 Ejemplo de Callable

```java
import java.util.concurrent.*;

public class EjemploCallable {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Callable que devuelve un Integer
        Callable<Integer> tarea = () -> {
            System.out.println("Calculando...");
            Thread.sleep(2000); // Simular trabajo pesado
            return 42; // Devolver resultado
        };

        // ¡Todavía no sabemos cómo obtener el resultado!
        // Lo veremos en la siguiente sección con Future

        executor.shutdown();
    }
}
```

---

## 4. Future: Resultados asíncronos

### 4.1 El concepto de Future

Un `Future<T>` es una **promesa de un valor futuro**. Es como un ticket de recogida:

**Analogía:**
- Vas a una tienda de reparaciones y dejas tu móvil
- Te dan un **ticket** (Future)
- Puedes irte y hacer otras cosas
- Más tarde, vuelves con el ticket y recoges tu móvil (resultado)

```java
Future<Integer> ticket = executor.submit(tareaCallable);
// ... haces otras cosas ...
Integer resultado = ticket.get(); // Recoger el resultado (se bloquea si no está listo)
```

### 4.2 Métodos de Future

| Método | Descripción |
|--------|-------------|
| `get()` | **Bloquea** hasta que la tarea termine y devuelve el resultado |
| `get(long timeout, TimeUnit unit)` | Espera como máximo X tiempo, lanza `TimeoutException` si no termina |
| `isDone()` | ¿Ya terminó la tarea? (true/false, sin bloquear) |
| `cancel(boolean mayInterruptIfRunning)` | Intenta cancelar la tarea |
| `isCancelled()` | ¿Fue cancelada? |

### 4.3 Ejemplo completo con Future

```java
import java.util.concurrent.*;

public class EjemploFuture {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Tarea que calcula el factorial
        Callable<Long> calcularFactorial = () -> {
            System.out.println("Calculando factorial de 20...");
            Thread.sleep(3000); // Simular cálculo pesado

            long resultado = 1;
            for (int i = 1; i <= 20; i++) {
                resultado *= i;
            }
            return resultado;
        };

        // Enviar la tarea y obtener el Future
        Future<Long> futureResultado = executor.submit(calcularFactorial);

        // Mientras tanto, el hilo principal hace otras cosas
        System.out.println("Tarea enviada, haciendo otras cosas...");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Hilo principal trabajando: " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Ahora necesitamos el resultado
        try {
            System.out.println("Esperando resultado...");
            Long resultado = futureResultado.get(); // BLOQUEA hasta tener resultado
            System.out.println("Factorial de 20 = " + resultado);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }
}
```

**Salida:**
```
Tarea enviada, haciendo otras cosas...
Calculando factorial de 20...
Hilo principal trabajando: 1
Hilo principal trabajando: 2
Hilo principal trabajando: 3
Hilo principal trabajando: 4
Hilo principal trabajando: 5
Esperando resultado...
Factorial de 20 = 2432902008176640000
```

### 4.4 Gestión de excepciones con Future

Si la tarea lanza una excepción, `get()` la envuelve en `ExecutionException`:

```java
Callable<Integer> tareaConError = () -> {
    System.out.println("Provocando error...");
    throw new IllegalArgumentException("¡Error intencional!");
};

Future<Integer> future = executor.submit(tareaConError);

try {
    Integer resultado = future.get();
} catch (ExecutionException e) {
    System.out.println("La tarea lanzó una excepción:");
    System.out.println(e.getCause()); // La excepción original
}
```

### 4.5 Timeout: No esperar para siempre

```java
Future<String> future = executor.submit(() -> {
    Thread.sleep(10000); // 10 segundos
    return "Terminado";
});

try {
    // Esperar máximo 2 segundos
    String resultado = future.get(2, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    System.out.println("¡La tarea tardó demasiado!");
    future.cancel(true); // Cancelar la tarea
}
```

### 4.6 Comprobar estado sin bloquear

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(5000);
    return 100;
});

// Polling (no recomendado en producción, pero educativo)
while (!future.isDone()) {
    System.out.println("Todavía calculando...");
    Thread.sleep(500);
}

Integer resultado = future.get(); // Ya está listo, no se bloquea
System.out.println("Resultado: " + resultado);
```

### 4.7 Múltiples Futures en paralelo

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

// Enviar múltiples tareas
Future<Integer> future1 = executor.submit(() -> {
    Thread.sleep(1000);
    return 10;
});

Future<Integer> future2 = executor.submit(() -> {
    Thread.sleep(2000);
    return 20;
});

Future<Integer> future3 = executor.submit(() -> {
    Thread.sleep(1500);
    return 30;
});

// Recoger todos los resultados
try {
    int suma = future1.get() + future2.get() + future3.get();
    System.out.println("Suma total: " + suma);
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}

executor.shutdown();
```

**Tiempo total:** ~2 segundos (el más lento), no 4.5 segundos (suma de todos).

---

## 5. BlockingQueue: Colas Thread-Safe

### 5.1 El problema con las colas normales

Imagina que tienes una cola normal (`Queue<T>` o `LinkedList<T>`) compartida entre hilos:

```java
Queue<String> cola = new LinkedList<>();

// Hilo productor
new Thread(() -> {
    cola.add("tarea");  // ¿Thread-safe? ❌ NO
}).start();

// Hilo consumidor
new Thread(() -> {
    String tarea = cola.poll();  // ¿Thread-safe? ❌ NO
}).start();
```

**Problemas:**
1. ❌ **No es thread-safe**: Múltiples hilos accediendo pueden corromper la cola
2. ❌ **Necesitas `synchronized` manual**: Código verboso y propenso a errores
3. ❌ **wait() / notify() manual**: Para esperar cuando la cola está vacía/llena
4. ❌ **Fácil cometer errores**: Deadlocks, condiciones de carrera, etc.

### 5.2 La solución: BlockingQueue

`BlockingQueue<E>` es una cola **thread-safe** que:
- ✅ **Bloquea automáticamente** cuando está vacía (al intentar sacar)
- ✅ **Bloquea automáticamente** cuando está llena (al intentar meter)
- ✅ **Thread-safe** sin necesidad de `synchronized`
- ✅ **Reemplaza `wait()` y `notify()`** automáticamente

**Analogía:** Una cinta transportadora en una fábrica:
- **Productor** (pone cajas en la cinta):
  - Si la cinta está llena → Espera automáticamente
- **Consumidor** (saca cajas de la cinta):
  - Si la cinta está vacía → Espera automáticamente

```java
BlockingQueue<String> cola = new ArrayBlockingQueue<>(10); // Capacidad: 10

// Productor
cola.put("tarea");  // Si está llena, ESPERA automáticamente

// Consumidor
String tarea = cola.take();  // Si está vacía, ESPERA automáticamente
```

### 5.3 Tipos de BlockingQueue

Java proporciona varias implementaciones:

| Clase | Descripción | Capacidad |
|-------|-------------|-----------|
| `ArrayBlockingQueue<E>` | Cola basada en array | **Fija** (debe especificarse) |
| `LinkedBlockingQueue<E>` | Cola basada en nodos enlazados | **Opcional** (por defecto ilimitada) |
| `PriorityBlockingQueue<E>` | Cola con prioridad | **Ilimitada** |
| `SynchronousQueue<E>` | Sin capacidad (handoff directo) | **0** (transferencia directa) |
| `DelayQueue<E>` | Elementos disponibles tras un delay | **Ilimitada** |

**Más comunes:**

#### A) ArrayBlockingQueue
```java
// Cola de capacidad fija (10 elementos)
BlockingQueue<Integer> cola = new ArrayBlockingQueue<>(10);
```
- ✅ Rendimiento predecible (array)
- ✅ Capacidad fija → Evita desbordes de memoria
- ❌ No puede crecer

#### B) LinkedBlockingQueue
```java
// Cola ilimitada (o con capacidad opcional)
BlockingQueue<Integer> cola = new LinkedBlockingQueue<>();
// O con límite:
BlockingQueue<Integer> colaLimitada = new LinkedBlockingQueue<>(100);
```
- ✅ Puede crecer dinámicamente
- ✅ Mejor para productores/consumidores a distinto ritmo
- ⚠️ Cuidado: Puede consumir mucha memoria si crece sin control

### 5.4 Métodos principales de BlockingQueue

| Método | Comportamiento si LLENA | Comportamiento si VACÍA | Devuelve |
|--------|-------------------------|-------------------------|----------|
| **put(e)** | **Espera** (bloquea) | - | void |
| **take()** | - | **Espera** (bloquea) | E |
| **offer(e)** | Devuelve `false` | - | boolean |
| **poll()** | - | Devuelve `null` | E |
| **offer(e, timeout, unit)** | Espera máx. X tiempo | - | boolean |
| **poll(timeout, unit)** | - | Espera máx. X tiempo | E |
| **add(e)** | Lanza `IllegalStateException` | - | boolean |
| **remove()** | - | Lanza `NoSuchElementException` | E |

**Métodos más usados:**
```java
// BLOQUEAN (espera automática)
cola.put(elemento);      // Añadir (espera si está llena)
Elemento e = cola.take(); // Sacar (espera si está vacía)

// NO BLOQUEAN (devuelven inmediatamente)
boolean ok = cola.offer(elemento);  // false si está llena
Elemento e = cola.poll();           // null si está vacía

// CON TIMEOUT (espera limitada)
boolean ok = cola.offer(elemento, 2, TimeUnit.SECONDS); // Espera máx. 2 seg
Elemento e = cola.poll(5, TimeUnit.SECONDS);            // Espera máx. 5 seg
```

### 5.5 Ejemplo básico: Productor-Consumidor

```java
import java.util.concurrent.*;

public class ProductorConsumidorBasico {

    public static void main(String[] args) {
        // Cola compartida (capacidad: 5)
        BlockingQueue<Integer> cola = new ArrayBlockingQueue<>(5);

        // PRODUCTOR: Produce números del 1 al 10
        Thread productor = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    System.out.println("📦 Produciendo: " + i);
                    cola.put(i);  // BLOQUEA si la cola está llena
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // CONSUMIDOR: Consume los números
        Thread consumidor = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Integer numero = cola.take();  // BLOQUEA si la cola está vacía
                    System.out.println("  ✅ Consumido: " + numero);
                    Thread.sleep(1000);  // Consume más lento que produce
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        productor.start();
        consumidor.start();
    }
}
```

**Salida esperada:**
```
📦 Produciendo: 1
  ✅ Consumido: 1
📦 Produciendo: 2
📦 Produciendo: 3
  ✅ Consumido: 2
📦 Produciendo: 4
📦 Produciendo: 5
📦 Produciendo: 6
  ✅ Consumido: 3
(El productor se bloquea aquí, cola llena con 4,5,6)
  ✅ Consumido: 4
📦 Produciendo: 7
...
```

**Observaciones:**
- El productor es **más rápido** (500ms) que el consumidor (1000ms)
- Cuando la cola se llena (5 elementos), el productor **espera automáticamente**
- No necesitamos `synchronized`, `wait()` ni `notify()` → BlockingQueue lo hace por nosotros

### 5.6 Ventajas vs synchronized manual

**ANTES (synchronized manual):**
```java
Queue<Integer> cola = new LinkedList<>();
Object lock = new Object();

// Productor
synchronized (lock) {
    while (cola.size() >= 5) {  // Cola llena
        lock.wait();             // Esperar
    }
    cola.add(elemento);
    lock.notifyAll();            // Despertar consumidores
}

// Consumidor
synchronized (lock) {
    while (cola.isEmpty()) {     // Cola vacía
        lock.wait();             // Esperar
    }
    Integer e = cola.remove();
    lock.notifyAll();            // Despertar productores
}
```
❌ **Problemas:** Verboso, propenso a errores, fácil olvidar `notifyAll()` → deadlock

**AHORA (BlockingQueue):**
```java
BlockingQueue<Integer> cola = new ArrayBlockingQueue<>(5);

// Productor
cola.put(elemento);  // ¡Una línea!

// Consumidor
Integer e = cola.take();  // ¡Una línea!
```
✅ **Ventajas:** Código limpio, thread-safe automático, sin deadlocks

### 5.7 Patrón Productor-Consumidor con múltiples hilos

```java
import java.util.concurrent.*;

public class MultipleProductorConsumidor {

    public static void main(String[] args) {
        BlockingQueue<String> cola = new ArrayBlockingQueue<>(10);

        // Crear 3 productores
        for (int i = 1; i <= 3; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        String tarea = "Tarea-P" + id + "-" + j;
                        cola.put(tarea);
                        System.out.println("📦 Productor-" + id + " produjo: " + tarea);
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Crear 2 consumidores
        for (int i = 1; i <= 2; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    while (true) {
                        String tarea = cola.take();
                        System.out.println("  ✅ Consumidor-" + id + " consumió: " + tarea);
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
```

**Resultado:** 3 productores y 2 consumidores trabajando en paralelo, coordinados automáticamente por la `BlockingQueue`.

### 5.8 BlockingQueue con ExecutorService

Combinación perfecta: `ExecutorService` + `BlockingQueue`

```java
import java.util.concurrent.*;

public class EjecutorConCola {

    public static void main(String[] args) {
        BlockingQueue<Runnable> cola = new ArrayBlockingQueue<>(5);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Productor: Añade tareas a la cola
        new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    int numeroTarea = i;
                    Runnable tarea = () -> {
                        System.out.println("Ejecutando tarea " + numeroTarea +
                            " en " + Thread.currentThread().getName());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    };
                    cola.put(tarea);  // Añadir tarea a la cola
                    System.out.println("📦 Tarea " + numeroTarea + " añadida a la cola");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Consumidor: Saca tareas de la cola y las ejecuta
        new Thread(() -> {
            try {
                while (true) {
                    Runnable tarea = cola.take();  // Sacar tarea de la cola
                    executor.submit(tarea);        // Ejecutar con el pool
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
```

### 5.9 Cuándo usar BlockingQueue

✅ **Usa BlockingQueue cuando:**
- Necesitas un **buffer** entre productores y consumidores
- Quieres **desacoplar** productores de consumidores (ritmo diferente)
- Implementas el **patrón Productor-Consumidor**
- Necesitas **thread-safety sin synchronized manual**
- Quieres **control de flujo** (evitar que productores desborden el sistema)

❌ **No uses BlockingQueue si:**
- No hay concurrencia (un solo hilo)
- Necesitas acceso aleatorio (no solo FIFO)
- La cola debe ser ordenada por prioridad (usa `PriorityBlockingQueue` en su lugar)

### 5.10 Resumen de conceptos clave

| Concepto | ANTES | AHORA |
|----------|-------|-------|
| **Cola compartida** | `Queue` + `synchronized` | `BlockingQueue` |
| **Esperar si vacía** | `wait()` manual | `take()` automático |
| **Esperar si llena** | `wait()` manual | `put()` automático |
| **Despertar hilos** | `notifyAll()` manual | Automático |
| **Thread-safety** | Manual con `synchronized` | Automático |
| **Código** | ~20 líneas | 2 líneas |
| **Errores** | Fácil (deadlock, race condition) | Difícil |

---

## 6. Ejemplos completos

### 5.1 Ejemplo: Descarga de archivos paralela

```java
import java.util.concurrent.*;
import java.util.*;

public class DescargaParalela {

    static class DescargadorArchivo implements Callable<String> {
        private String url;

        public DescargadorArchivo(String url) {
            this.url = url;
        }

        @Override
        public String call() throws Exception {
            System.out.println("Descargando " + url + "...");

            // Simular descarga (tiempo aleatorio)
            int tiempoDescarga = (int) (Math.random() * 3000) + 1000;
            Thread.sleep(tiempoDescarga);

            return "✓ " + url + " descargado en " + tiempoDescarga + "ms";
        }
    }

    public static void main(String[] args) {
        List<String> urls = Arrays.asList(
            "https://ejemplo.com/archivo1.zip",
            "https://ejemplo.com/archivo2.zip",
            "https://ejemplo.com/archivo3.zip",
            "https://ejemplo.com/archivo4.zip",
            "https://ejemplo.com/archivo5.zip"
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<String>> futuros = new ArrayList<>();

        // Enviar todas las descargas
        for (String url : urls) {
            Future<String> futuro = executor.submit(new DescargadorArchivo(url));
            futuros.add(futuro);
        }

        // Recoger resultados
        System.out.println("\n--- RESULTADOS ---");
        for (Future<String> futuro : futuros) {
            try {
                String resultado = futuro.get();
                System.out.println(resultado);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error en descarga: " + e.getMessage());
            }
        }

        executor.shutdown();
    }
}
```

### 5.2 Ejemplo: Cálculo paralelo de números primos

```java
import java.util.concurrent.*;
import java.util.*;

public class CalculadorPrimos {

    static class BuscadorPrimos implements Callable<List<Integer>> {
        private int inicio;
        private int fin;

        public BuscadorPrimos(int inicio, int fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        public List<Integer> call() {
            List<Integer> primos = new ArrayList<>();
            for (int num = inicio; num <= fin; num++) {
                if (esPrimo(num)) {
                    primos.add(num);
                }
            }
            return primos;
        }

        private boolean esPrimo(int n) {
            if (n < 2) return false;
            for (int i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) return false;
            }
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        int rangoTotal = 100000;
        int numeroHilos = 4;
        int rangoPorHilo = rangoTotal / numeroHilos;

        ExecutorService executor = Executors.newFixedThreadPool(numeroHilos);
        List<Future<List<Integer>>> futuros = new ArrayList<>();

        long inicio = System.currentTimeMillis();

        // Dividir el trabajo en 4 partes
        for (int i = 0; i < numeroHilos; i++) {
            int inicioRango = i * rangoPorHilo;
            int finRango = (i == numeroHilos - 1) ? rangoTotal : (i + 1) * rangoPorHilo - 1;

            Future<List<Integer>> futuro = executor.submit(
                new BuscadorPrimos(inicioRango, finRango)
            );
            futuros.add(futuro);
        }

        // Recoger todos los primos
        List<Integer> todosPrimos = new ArrayList<>();
        for (Future<List<Integer>> futuro : futuros) {
            todosPrimos.addAll(futuro.get());
        }

        long fin = System.currentTimeMillis();

        System.out.println("Primos encontrados: " + todosPrimos.size());
        System.out.println("Tiempo: " + (fin - inicio) + "ms");
        System.out.println("Primeros 10 primos: " + todosPrimos.subList(0, 10));

        executor.shutdown();
    }
}
```

### 5.3 Ejemplo: Patrón de tareas con timeout

```java
import java.util.concurrent.*;

public class TareaConTimeout {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> tareaLenta = () -> {
            System.out.println("Iniciando tarea lenta...");
            Thread.sleep(5000); // 5 segundos
            return "Tarea completada";
        };

        Future<String> future = executor.submit(tareaLenta);

        try {
            // Esperar máximo 2 segundos
            String resultado = future.get(2, TimeUnit.SECONDS);
            System.out.println(resultado);

        } catch (TimeoutException e) {
            System.out.println("⏰ Timeout: La tarea tardó más de 2 segundos");
            future.cancel(true); // Interrumpir la tarea

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
```

---

## 6. Ejercicios propuestos

### Ejercicio 1: Conversor de temperaturas paralelo
Crea un programa que convierta una lista de temperaturas de Celsius a Fahrenheit usando un `FixedThreadPool` de 2 hilos. Cada conversión debe ser un `Callable<Double>` que devuelva el resultado.

**Lista de entrada:** `[0, 10, 20, 30, 40, 50, 100]`

### Ejercicio 2: Simulador de cajero bancario
Simula un banco con 3 cajeros (hilos) atendiendo a 15 clientes. Cada cliente es una tarea que tarda entre 1-3 segundos. Usa `Runnable` y muestra qué cajero atiende a cada cliente.

### Ejercicio 3: Buscador de palabras en archivos
Dado un directorio con múltiples archivos `.txt`, crea una tarea `Callable<Integer>` por cada archivo que cuente cuántas veces aparece una palabra. Usa `Future` para obtener el total de coincidencias sumando todos los archivos.

### Ejercicio 4: Timeout de API externa
Simula una llamada a una API externa que puede tardar tiempo impredecible (usa `Thread.sleep` con tiempo aleatorio 1-10 segundos). Implementa un timeout de 3 segundos: si no responde, cancela la tarea y muestra un mensaje de error.

### Ejercicio 5: Calculadora paralela
Crea un programa que calcule en paralelo:
- Suma de números del 1 al 1.000.000
- Producto de números del 1 al 20
- Cantidad de números pares del 1 al 1.000.000

Cada operación debe ser un `Callable` diferente. Usa un `FixedThreadPool` de 3 hilos y mide el tiempo total de ejecución.

---

## 7. Resumen y buenas prácticas

### ✅ Cuándo usar cada tipo de pool

| Situación | Pool recomendado |
|-----------|------------------|
| Conoces el número de tareas y quieres control | `FixedThreadPool` |
| Muchas tareas cortas y asíncronas | `CachedThreadPool` |
| Necesitas orden estricto (secuencial) | `SingleThreadExecutor` |
| Tareas programadas o periódicas | `ScheduledThreadPool` |

### ✅ Runnable vs Callable

- Usa `Runnable` si la tarea no devuelve resultado (ej: guardar en log, enviar email)
- Usa `Callable` si necesitas el resultado (ej: cálculos, consultas a BD, descargas)

### ✅ Buenas prácticas

1. **Siempre cierra el Executor**: Usa `shutdown()` o try-with-resources
2. **Gestiona excepciones**: Captura `ExecutionException` al hacer `future.get()`
3. **Usa timeouts**: No confíes en que las tareas siempre terminen rápido
4. **Tamaño del pool**:
   - CPU-bound (cálculos): `nCores` o `nCores + 1`
   - I/O-bound (red, disco): `nCores * 2` o más
5. **Nombra tus pools** (avanzado):
```java
ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
    .setNameFormat("miPool-%d").build();
ExecutorService executor = Executors.newFixedThreadPool(4, namedThreadFactory);
```

### ⚠️ Errores comunes

1. ❌ No cerrar el executor → El programa no termina
2. ❌ Usar `CachedThreadPool` con tareas largas → Crear miles de hilos
3. ❌ No capturar excepciones de `future.get()` → Perder errores
4. ❌ Hacer `get()` inmediatamente → Bloquear el hilo principal (pierde el sentido de la asincronía)

---

## 8. Comparativa final: Antes y después

### ANTES (gestión manual):
```java
for (int i = 0; i < 100; i++) {
    Thread hilo = new Thread(() -> {
        calcularAlgo();
    });
    hilo.start();
}
// ¡100 hilos creados! Sistema sobrecargado
```

### DESPUÉS (con Executors):
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < 100; i++) {
    executor.submit(() -> calcularAlgo());
}
executor.shutdown();
// Solo 4 hilos, reutilizados para las 100 tareas
```

---

## 9. Siguiente paso: CompletableFuture (opcional/avanzado)

Una vez dominéis `Future`, existe una API aún más moderna: **`CompletableFuture`** (Java 8+).

Permite:
- Encadenar tareas (thenApply, thenCompose)
- Combinar múltiples futures (allOf, anyOf)
- Gestión de excepciones más elegante

Pero eso es material para otro día. Primero domina Executors y Future.

---

## 10. Recursos adicionales

- Documentación oficial Java: [java.util.concurrent](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/package-summary.html)
- Libro: "Java Concurrency in Practice" de Brian Goetz (LA biblia)
- Tutorial Oracle: [Executors](https://docs.oracle.com/javase/tutorial/essential/concurrency/executors.html)

---

**¡Fin del documento! Ahora a practicar con los ejemplos y ejercicios.**
