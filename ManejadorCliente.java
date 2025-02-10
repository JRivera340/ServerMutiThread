import java.io.*;
import java.net.*;
import java.util.*;

// Clase para manejar cada cliente en un hilo separado
public class ManejadorCliente extends Thread {
    private Socket socketCliente;

    public ManejadorCliente(Socket socket) {
        this.socketCliente = socket;
    }

    public void run() {
        try (
            BufferedReader mensajeDesdeCliente = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            DataOutputStream mensajeParaCliente = new DataOutputStream(socketCliente.getOutputStream())
        ) {
            // Leer la primera línea de la solicitud HTTP
            String lineaDeLaSolicitudHttp = mensajeDesdeCliente.readLine();
            if (lineaDeLaSolicitudHttp == null) return;

            System.out.println("Solicitud HTTP recibida: " + lineaDeLaSolicitudHttp);

            StringTokenizer lineaSeparada = new StringTokenizer(lineaDeLaSolicitudHttp);
            String metodo = lineaSeparada.nextToken(); // GET
            String nombreArchivo = lineaSeparada.nextToken(); // /index.html

            // Ignorar el resto de la solicitud HTTP
            while (mensajeDesdeCliente.ready()) {
                mensajeDesdeCliente.readLine();
            }

            // Validar que el método sea GET
            if (!metodo.equals("GET")) {
                enviarError(mensajeParaCliente, "400 Bad Request");
                return;
            }

            // Quitar la barra inicial si existe
            if (nombreArchivo.startsWith("/")) {
                nombreArchivo = nombreArchivo.substring(1);
            }
            
            if (nombreArchivo.isEmpty()) {
                nombreArchivo = "index.html"; 
            }

            File archivo = new File(nombreArchivo);
            if (!archivo.exists() || archivo.isDirectory()) {
                enviarError(mensajeParaCliente, "404 Not Found");
                return;
            }

            // Leer el archivo y enviar respuesta HTTP
            enviarArchivo(mensajeParaCliente, archivo);
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                socketCliente.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

    // Método para enviar mensajes de error al cliente
    private void enviarError(DataOutputStream mensajeParaCliente, String mensaje) throws IOException {
        mensajeParaCliente.writeBytes("HTTP/1.0 " + mensaje + "\r\n");
        mensajeParaCliente.writeBytes("Content-Type: text/html\r\n");
        mensajeParaCliente.writeBytes("\r\n");
        mensajeParaCliente.writeBytes("<html><body><h1>" + mensaje + "</h1></body></html>\r\n");
    }

    // Método para enviar un archivo solicitado
    private void enviarArchivo(DataOutputStream mensajeParaCliente, File archivo) throws IOException {
        FileInputStream archivoDeEntrada = new FileInputStream(archivo);
        int cantidadDeBytes = (int) archivo.length();
        byte[] archivoEnBytes = new byte[cantidadDeBytes];

        archivoDeEntrada.read(archivoEnBytes);
        archivoDeEntrada.close();

        // Construcción de la respuesta HTTP
        mensajeParaCliente.writeBytes("HTTP/1.0 200 OK\r\n");
        mensajeParaCliente.writeBytes("Content-Length: " + cantidadDeBytes + "\r\n");
        mensajeParaCliente.writeBytes("Content-Type: " + obtenerTipoContenido(archivo.getName()) + "\r\n");
        mensajeParaCliente.writeBytes("\r\n");

     
        mensajeParaCliente.write(archivoEnBytes, 0, cantidadDeBytes);
    }

    
    private String obtenerTipoContenido(String nombreArchivo) {
        if (nombreArchivo.endsWith(".html")) return "text/html";
        if (nombreArchivo.endsWith(".jpg")) return "image/jpeg";
        if (nombreArchivo.endsWith(".gif")) return "image/gif";
        if (nombreArchivo.endsWith(".png")) return "image/png";
        if (nombreArchivo.endsWith(".css")) return "text/css";
        if (nombreArchivo.endsWith(".js")) return "application/javascript";
        return "application/octet-stream";
    }
}
