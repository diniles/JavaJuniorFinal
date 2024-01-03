package server;

import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private final int port;

    public ChatServer(int port) {
        this.port = port;
    }

    void start() {
        System.out.println("Starting server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            System.out.printf("Error server on port %d: %s \n", port, e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ChatServer(8182).start();
    }
}
