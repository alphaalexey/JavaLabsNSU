package ru.alspace;

public class Main {
    public static void main(String[] args) {
        int port;
        boolean useSerialization = false;

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port");
                return;
            }
            if (args.length >= 2 && "ser".equalsIgnoreCase(args[1])) {
                useSerialization = true;
            }
        } else {
            port = 12345;
        }

        ChatServer server = new ChatServer(port, useSerialization);
        server.start();
    }
}
