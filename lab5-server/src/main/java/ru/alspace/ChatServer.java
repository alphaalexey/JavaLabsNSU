package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {
    private static final Logger logger = LogManager.getLogger(ChatServer.class);
    private final int port;
    private final boolean useSerialization; // true – использовать сериализацию, false – XML
    // Храним активные сессии (ключ – уникальный sessionId)
    private final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public ChatServer(int port, boolean useSerialization) {
        this.port = port;
        this.useSerialization = useSerialization;
    }

    public void start() {
        logger.info("Запуск сервера на порту: {}", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Подключился клиент: {}", clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this, useSerialization);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            logger.error("Ошибка сервера", e);
        }
    }

    // Регистрация клиента (по sessionId)
    public synchronized boolean registerClient(String sessionId, ClientHandler clientHandler) {
        if (clients.containsKey(sessionId)) {
            return false;
        }
        clients.put(sessionId, clientHandler);
        broadcastUserLogin(clientHandler.getUserName());
        return true;
    }

    public synchronized void removeClient(String sessionId, String userName) {
        clients.remove(sessionId);
        broadcastUserLogout(userName);
    }

    // Отправка сообщения всем подключённым клиентам
    public synchronized void broadcastMessage(String message, String fromUser) {
        for (ClientHandler client : clients.values()) {
            client.sendChatMessage(fromUser, message);
        }
    }

    public synchronized List<String> getUserList() {
        List<String> users = new ArrayList<>();
        for (ClientHandler client : clients.values()) {
            users.add(client.getUserName());
        }
        return users;
    }

    // Рассылка события о входе нового клиента
    private void broadcastUserLogin(String userName) {
        for (ClientHandler client : clients.values()) {
            client.sendUserLoginEvent(userName);
        }
    }

    // Рассылка события об отключении клиента
    private void broadcastUserLogout(String userName) {
        for (ClientHandler client : clients.values()) {
            client.sendUserLogoutEvent(userName);
        }
    }
}
