package client;

public class ClientTwo {
    public static void main(String[] args) {
        new ChatClient("localhost", 8182, "ClientTwo").start();
    }
}
