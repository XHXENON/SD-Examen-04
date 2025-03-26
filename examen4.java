/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package examen3sd;

import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * @author javierluna
 */
public class Examen3SD {

    static class Matriz {
        int[][] datos;
        int tamano;

        Matriz(int n) {
            tamano = n;
            datos = new int[n][n];
        }
    }

    static class MultiplicadorMatriz implements Runnable {
        private final Matriz A, B, C;
        private final int filaInicio, filaFin;

        MultiplicadorMatriz(Matriz A, Matriz B, Matriz C, int filaInicio, int filaFin) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.filaInicio = filaInicio;
            this.filaFin = filaFin;
        }

        @Override
        public void run() {
            for (int i = filaInicio; i < filaFin; i++)
                for (int j = 0; j < A.tamano; j++)
                    for (int k = 0; k < A.tamano; k++)
                        C.datos[i][j] += A.datos[i][k] * B.datos[k][j];
        }
    }

    // Inicializar matrices con valores aleatorios
    static void inicializarMatrices(Matriz A, Matriz B, Matriz C) {
        Random rand = new Random();
        for (int i = 0; i < A.tamano; i++) {
            for (int j = 0; j < A.tamano; j++) {
                A.datos[i][j] = rand.nextInt(6);
                B.datos[i][j] = rand.nextInt(6);
                C.datos[i][j] = 0;
            }
        }
    }

    // Multiplicación de matrices con 3 hilos
    static double multiplicarConHilos(Matriz A, Matriz B, Matriz C) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        int tamanoChunk = A.tamano / 3;
        Future<?>[] futuros = new Future[3];

        long inicio = System.nanoTime();

        for (int i = 0; i < 3; i++) {
            int filaInicio = i * tamanoChunk;
            int filaFin = (i == 2) ? A.tamano : (i + 1) * tamanoChunk;
            futuros[i] = executor.submit(new MultiplicadorMatriz(A, B, C, filaInicio, filaFin));
        }

        for (Future<?> futuro : futuros) {
            futuro.get();
        }

        long fin = System.nanoTime();
        executor.shutdown();
        return (fin - inicio) / 1e9; // Convertir a segundos
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int[] tamanos = {100, 500, 1000};
        int ejecuciones = 5;

        for (int tamano : tamanos) {
            System.out.println("\n=== Resultados para matrices " + tamano + "x" + tamano + " ===");
            Matriz A = new Matriz(tamano);
            Matriz B = new Matriz(tamano);
            Matriz C = new Matriz(tamano);

            double[] tiempos = new double[ejecuciones];
            double tiempoLlenado = 0;

            for (int ejecucion = 0; ejecucion < ejecuciones; ejecucion++) {
                // Medir tiempo de llenado
                long inicioLlenado = System.nanoTime();
                inicializarMatrices(A, B, C);
                long finLlenado = System.nanoTime();
                tiempoLlenado += (finLlenado - inicioLlenado) / 1e9;

                // Medir tiempo de multiplicación
                tiempos[ejecucion] = multiplicarConHilos(A, B, C);
                System.out.printf("Ejecución %d: %.6f segundos%n", ejecucion + 1, tiempos[ejecucion]);
            }

            // Calcular estadísticas
            double tiempoPromedio = 0;
            double tiempoMinimo = Double.MAX_VALUE;
            double tiempoMaximo = 0;
            for (double tiempo : tiempos) {
                tiempoPromedio += tiempo;
                tiempoMinimo = Math.min(tiempoMinimo, tiempo);
                tiempoMaximo = Math.max(tiempoMaximo, tiempo);
            }
            tiempoPromedio /= ejecuciones;

            // Mostrar resultados
            System.out.printf("Tiempo promedio de llenado: %.6f segundos%n", tiempoLlenado / ejecuciones);
            System.out.printf("Tiempo promedio de multiplicación: %.6f segundos%n", tiempoPromedio);
            System.out.printf("Tiempo mínimo: %.6f segundos, Tiempo máximo: %.6f segundos%n", tiempoMinimo, tiempoMaximo);
        }
    }
}