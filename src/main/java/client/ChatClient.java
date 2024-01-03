package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private BufferedWriter out;
    private BufferedReader in;
    private Socket socket;

    public ChatClient(String host, int port, String clientName) {
        try {
            socket = new Socket(host, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.printf("Connected to server %s:%d\nType 'exit' to disconnect\n", host, port);
            System.out.printf("InetAddress: %s, local port %d\n", socket.getInetAddress().getHostAddress(), socket.getLocalPort());
            sendMessage(clientName);
        } catch (IOException e) {
            System.out.printf("Error connecting to server %s:%d: %s\n", host, port, e.getMessage());
            closeAll();
        }
    }

    void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.out.printf("Error sending message %s: %s\n", message, e.getMessage());
            closeAll();
        }
    }

    void closeAll() {
        try {
            in.close();
            out.flush();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.printf("Error closing connection: %s\n", e.getMessage());
        }
    }

    void messageListener() {
        new Thread(() -> {
            try {
                String serverResponse;
                while (socket.isConnected()) {
                    serverResponse = in.readLine();
                    if (serverResponse == null) {
                        System.out.println("Server disconnected");
                        closeAll();
                        break;
                    }
                    System.out.print(serverResponse + "\n> ");
                }
            } catch (IOException e) {
                System.out.printf("Server IOException in thread: %s\n", e.getMessage());
                closeAll();
            }
        }).start();
    }

    public void start() {
        messageListener();
        String userInput;
        Scanner scanner = new Scanner(System.in);
        try {
            while (socket.isConnected()) {
                System.out.print("> ");
                userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.printf("Close command from user %s\n", socket.getInetAddress().getHostAddress());
                    closeAll();
                    break;
                }
                sendMessage(userInput);
            }
        } catch (Exception e) {
            System.out.printf("Error sending message: %s\n", e.getMessage());
            closeAll();
        }

    }
}
