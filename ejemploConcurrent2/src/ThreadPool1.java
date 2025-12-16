/*
EJERCICIO 1: Conversor de temperaturas paralelo

OBJETIVO:
Convertir una lista de temperaturas de Celsius a Fahrenheit usando
un FixedThreadPool de 2 hilos. Cada conversión debe ser un Callable<Double>.

FÓRMULA: F = C * 9/5 + 32

LISTA DE ENTRADA: [0, 10, 20, 30, 40, 50, 100]

REQUISITOS:
1. Crear un Callable<Double> que convierta una temperatura
2. Usar ExecutorService con FixedThreadPool de 2 hilos
3. Usar Future para recoger los resultados
4. Mostrar tabla con Celsius -> Fahrenheit
5. Cerrar correctamente el executor
*/

import java.util.*;
import java.util.concurrent.*;

public class ThreadPool1 {

    // TODO: Crear clase ConversorTemperatura que implemente Callable<Double>
    static class ConversorTemperatura implements Callable<Double> {
        private double celsius;

        public ConversorTemperatura(double celsius) {
            this.celsius = celsius;
        }

        @Override
        public Double call() throws Exception {
            // Simular que la conversión tarda un poco (100ms)
            Thread.sleep(100);
            // Retornar el resultado en Fahrenheit
            return celsius*9/5+32;
        }
    }

    public static void main(String[] args) {
        System.out.println("\n=== CONVERSOR DE TEMPERATURAS PARALELO ===\n");

        // Lista de temperaturas en Celsius
        List<Double> temperaturasCelsius = Arrays.asList(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 100.0);

        // TODO: Crear ExecutorService con FixedThreadPool de 2 hilos
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // TODO: Crear lista para almacenar los Futures
        List<Future<Double>> futuros = new ArrayList<>();

        // TODO: Enviar todas las tareas de conversión
        for (Double celsius : temperaturasCelsius){
            Callable<Double> tarea=new ConversorTemperatura(celsius);
            Future<Double> futuro= executor.submit(tarea);
            futuros.add(futuro);
        }

        // TODO: Recoger todos los resultados y mostrarlos en una tabla
        System.out.println("╔═══════════╦═════════════╗");
        System.out.println("║  Celsius  ║  Fahrenheit ║");
        System.out.println("╠═══════════╬═════════════╣");

        for (int i = 0; i < futuros.size(); i++) {
            try {
                double celsius=temperaturasCelsius.get(i);
                double fahrenheit=futuros.get(i).get();
                System.out.printf("║   %6.1f  ║    %6.1f   ║%n", celsius, fahrenheit);
            } catch (InterruptedException | ExecutionException e){
                System.out.println("Error al obtener el resultado: "+e.getMessage());
            }
        }

        System.out.println("╚═══════════╩═════════════╝\n");

        // TODO: Cerrar el executor correctamente
        executor.shutdown();
        System.out.println("✅ Conversión completada\n");
    }
}
