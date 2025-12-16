/**
 * EJERCICIO 1: Conversor de temperaturas paralelo
 *
 * OBJETIVO:
 * Convertir una lista de temperaturas de Celsius a Fahrenheit usando
 * un FixedThreadPool de 2 hilos. Cada conversión debe ser un Callable<Double>.
 *
 * FÓRMULA: F = C * 9/5 + 32
 *
 * LISTA DE ENTRADA: [0, 10, 20, 30, 40, 50, 100]
 *
 * REQUISITOS:
 * 1. Crear un Callable<Double> que convierta una temperatura
 * 2. Usar ExecutorService con FixedThreadPool de 2 hilos
 * 3. Usar Future para recoger los resultados
 * 4. Mostrar tabla con Celsius -> Fahrenheit
 * 5. Cerrar correctamente el executor
 */

import java.util.*;
import java.util.concurrent.*;

public class Ejercicio01_PLANTILLA {

    // TODO: Crear clase ConversorTemperatura que implemente Callable<Double>
    static class ConversorTemperatura implements Callable<Double> {
        private double celsius;

        public ConversorTemperatura(double celsius) {
            this.celsius = celsius;
        }

        @Override
        public Double call() throws Exception {
            // TODO: Implementar conversión
            // Simular que la conversión tarda un poco (100ms)
            // Retornar el resultado en Fahrenheit

            return 0.0; // CAMBIAR ESTO
        }
    }

    public static void main(String[] args) {
        System.out.println("\n=== CONVERSOR DE TEMPERATURAS PARALELO ===\n");

        // Lista de temperaturas en Celsius
        List<Double> temperaturasCelsius = Arrays.asList(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 100.0);

        // TODO: Crear ExecutorService con FixedThreadPool de 2 hilos
        ExecutorService executor = null; // CAMBIAR ESTO

        // TODO: Crear lista para almacenar los Futures
        List<Future<Double>> futuros = new ArrayList<>();

        // TODO: Enviar todas las tareas de conversión
        // for (Double celsius : temperaturasCelsius) {
        //     ...
        // }

        // TODO: Recoger todos los resultados y mostrarlos en una tabla
        System.out.println("╔═══════════╦═════════════╗");
        System.out.println("║  Celsius  ║  Fahrenheit ║");
        System.out.println("╠═══════════╬═════════════╣");

        // for (int i = 0; i < futuros.size(); i++) {
        //     try {
        //         ...
        //         System.out.printf("║   %6.1f  ║    %6.1f   ║%n", celsius, fahrenheit);
        //     } catch (...) {
        //         ...
        //     }
        // }

        System.out.println("╚═══════════╩═════════════╝\n");

        // TODO: Cerrar el executor correctamente

        System.out.println("✅ Conversión completada\n");
    }
}

/*
 * SALIDA ESPERADA:
 *
 * === CONVERSOR DE TEMPERATURAS PARALELO ===
 *
 * ╔═══════════╦═════════════╗
 * ║  Celsius  ║  Fahrenheit ║
 * ╠═══════════╬═════════════╣
 * ║      0.0  ║      32.0   ║
 * ║     10.0  ║      50.0   ║
 * ║     20.0  ║      68.0   ║
 * ║     30.0  ║      86.0   ║
 * ║     40.0  ║     104.0   ║
 * ║     50.0  ║     122.0   ║
 * ║    100.0  ║     212.0   ║
 * ╚═══════════╩═════════════╝
 *
 * ✅ Conversión completada
 */
