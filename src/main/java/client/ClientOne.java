package client;

public class ClientOne {
    public static void main(String[] args) {
        new ChatClient("localhost", 8182, "ClientOne").start();
    }
}
