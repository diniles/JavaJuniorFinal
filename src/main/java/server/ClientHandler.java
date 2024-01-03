package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientList = new ArrayList<>();
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientName = in.readLine();
            clientList.add(this);
            broadcastMessage("User " + clientName + " has joined the chat");
        } catch (IOException e) {
            System.out.printf("Error connecting client %s:%d: %s\n", socket.getInetAddress().getHostAddress(), socket.getPort(), e.getMessage());
            closeAll();
            throw new RuntimeException(e);
        }
    }

    void sendMessage(String message, String clientName) {
        clientList.stream().filter(client -> client.clientName != null).filter(client -> client.clientName.equals(clientName)).forEach(client -> {
            try {
                client.out.write(message + "\n");
                client.out.flush();
            } catch (IOException e) {
                System.out.printf("Error sending message %s: %s\n", message, e.getMessage());
                closeAll();
            }
        });
    }

    void broadcastMessage(String message) {
        clientList.stream().filter(client -> !client.clientName.equals(this.clientName)).forEach(client -> {
            try {
                System.out.printf("Sending message to %s: %s\n", client.clientName, message);
                client.out.write(message + "\n");
                client.out.flush();
            } catch (IOException e) {
                System.out.printf("Error broadcast message %s: %s\n", message, e.getMessage());
                closeAll();
            }
        });
        System.out.printf("Message sent: %s\n", message);
    }


    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String message = in.readLine();
                if (message == null) {
                    System.out.println("Client null message");
                    closeAll();
                    break;
                }
                if (message.startsWith("@")) {
                    String[] messageParts = message.split(" ", 2);
                    String toName = messageParts[0].substring(1);
                    String messageBody = messageParts[1];
                    sendMessage(clientName + " private message: " + messageBody, toName);
                } else {
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                closeAll();
                break;
            }
        }
        broadcastMessage("User " + clientName + " has left the chat");
    }

    void closeAll() {
        clientList.remove(this);
        try {
            broadcastMessage("User " + clientName + " has left the chat");
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
