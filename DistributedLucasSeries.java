import java.util.Scanner;
import java.util.Random;


import java.util.Scanner;

public class DistributedLucasSeries {

    private static int terminoActual = 0;  // Variable compartida entre los hilos
    private static int[] serieLucas; // Para almacenar la serie de Lucas de manera lineal
    private static volatile int[] relojesLamport; // Relojes de Lamport

    public static void main(String[] args) {
        /*
         * Solicitar al usuario el n�mero de hilos a usar y el n�mero de t�rminos de la
         * serie de Lucas a calcular
         * 
         * return: void
         */
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el n�mero de hilos a usar: ");
        int numHilos = scanner.nextInt();

        System.out.print("Ingrese el n�mero de t�rminos de la serie de Lucas a calcular: ");
        int n = scanner.nextInt();

        System.out.print("Le gustaria simular un fallo s(Si) - n(No): ");
        char fallo = scanner.next().charAt(0);
        scanner.close();

        serieLucas = new int[n]; // Inicializar el arreglo para almacenar la serie
        relojesLamport = new int[numHilos]; // Inicializar el arreglo para los relojes de Lamport

        calcularSerieLucasParalela(numHilos, n, fallo);

        // Imprimir la serie de manera lineal al finalizar
        System.out.print("Serie de Lucas: ");
        for (int i = 0; i < n; i++) {
            System.out.print(serieLucas[i] + " ");
        }
    }

    private static void calcularSerieLucasParalela(int numHilos, int n, char fallo) {
        /*
         * Crear y ejecutar los hilos para calcular la serie de Lucas de manera paralela
         * usando el n�mero de hilos especificado
         * 
         * arg: numHilos - El n�mero de hilos a usar
         * arg: n - El n�mero de t�rminos de la serie de Lucas a calcular
         * 
         * return: void
         */
        Thread[] hilos = new Thread[numHilos];

        for (int i = 0; i < numHilos; i++) {
            final int hiloNumero = i;

            hilos[i] = new Thread(() -> calcularPorcionSerieLucasLamport(hiloNumero, n));
            hilos[i].start();
        }
        
        // Introducimos un temporizador para simular un fallo de hilo
        try {
            Thread.sleep(300); // Esperar 0.3 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Simulamos un fallo en uno de los hilos
        if (fallo == 's') {
            Random random = new Random();

        // Generar un n�mero entero aleatorio entre 0 y la cantidad maxima de los hilos
            int randomNumber = random.nextInt(numHilos);
            hilos[randomNumber].interrupt(); // Simulamos que el hilo falla
            System.out.println("Fallo del Hilo " + randomNumber);
        }

        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void calcularPorcionSerieLucasLamport(int hiloNumero, int n) {
        /*
         * Calcular una porci�n de la serie de Lucas de manera secuencial, usando el
         * reloj de Lamport para sincronizar la impresi�n de los resultados
         * 
         * arg: hiloNumero - El n�mero del hilo actual
         * arg: n - El n�mero de t�rminos de la serie de Lucas a calcular
         * 
         * return: void
         */
        while (true) {
            int indiceActual;
            int tiempoLamportInicio = obtenerTiempoLamport(hiloNumero); // Obtener el tiempo al inicio del c�lculo

            synchronized (relojesLamport) {
                indiceActual = terminoActual;
                terminoActual++;
            }

            if (indiceActual < n) {
                int valorTermino = calcularTerminoSerieLucas(indiceActual);
                serieLucas[indiceActual] = valorTermino;

                synchronized (relojesLamport) {
                    System.out.println("Hilo " + hiloNumero + " - Termino " + indiceActual + ": " +
                            valorTermino + " - Reloj Lamport: " + tiempoLamportInicio);
                }

                try {
                    Thread.sleep(500); // Simulaci�n del tiempo de ejecuci�n del hilo
                } catch (InterruptedException e) {
                    // Manejar la interrupci�n para la finalizaci�n del hilo
                    System.out.println("Hilo " + hiloNumero + " interrumpido.");
                    break;
                }
            } else {
                break; // El hilo termina cuando se alcanza el final de la serie
            }
        }
    }

    private static int calcularTerminoSerieLucas(int termino) {
        /*
         * Calcular el valor de un t�rmino de la serie de Lucas
         * 
         * arg: termino - El n�mero del t�rmino a calcular
         * return: El valor del t�rmino
         */
        if (termino == 0) {
            return 2;
        } else if (termino == 1) {
            return 1;
        } else {
            int terminoAnterior1 = 2;
            int terminoAnterior2 = 1;
            int siguienteTermino = 0;

            for (int i = 2; i <= termino; i++) {
                siguienteTermino = terminoAnterior1 + terminoAnterior2;
                terminoAnterior1 = terminoAnterior2;
                terminoAnterior2 = siguienteTermino;
            }

            return siguienteTermino;
        }
    }

    private static int obtenerTiempoLamport(int hiloNumero) {
        /*
         * Obtener el tiempo actual del reloj de Lamport para el hilo especificado
         * 
         * arg: hiloNumero - El n�mero del hilo
         * return: El tiempo actual del reloj de Lamport para el hilo
         */
        return relojesLamport[hiloNumero]++;
    }
}