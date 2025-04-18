package ru.alspace;

public class Main {
    public static void main(String[] args) {
        int port;
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.exit(1);
                return;
            }
        } else if (args.length == 0) {
            port = 12345;
        } else {
            System.exit(1);
            return;
        }
        boolean useSerialization = false; // false - XML, true - объектная сериализация
        ChatServer server = new ChatServer(port, useSerialization);
        server.start();
    }
}
