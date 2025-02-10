import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorWebMultiHilos {
    public static void main(String[] args) throws Exception {
        int puerto = 6789;
        ServerSocket socketServidor = new ServerSocket(puerto);
        System.out.println("Servidor Web Multi-Hilos iniciado en el puerto " + puerto);

        while (true) {
            try {
                // Acepta la conexión de un cliente
                Socket socketCliente = socketServidor.accept();
                System.out.println("Nueva conexión establecida");

                // Crea un hilo para manejar la solicitud del cliente
                new ManejadorCliente(socketCliente).start();
            } catch (IOException e) {
                System.err.println("Error al aceptar conexión: " + e.getMessage());
            }
        }
    }
}


