package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.common.model.data.ChatMessage;
import ru.alspace.common.model.event.MessageEvent;
import ru.alspace.common.model.event.UserLoginEvent;
import ru.alspace.common.model.event.UserLogoutEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.*;

public class ChatServer {
    private static final Logger logger = LogManager.getLogger(ChatServer.class);

    private final int port;
    private final boolean useSerialization;
    private final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    private final List<ChatMessage> history = Collections.synchronizedList(new ArrayList<>());

    public ChatServer(int port, boolean useSerialization) {
        this.port = port;
        this.useSerialization = useSerialization;
    }

    public void start() {
        logger.info("Starting server on port {} (serialization={})", port, useSerialization);
        try (ServerSocket ss = new ServerSocket(port)) {
            while (true) {
                Socket sock = ss.accept();
                logger.info("Client connected: {}", sock.getInetAddress());
                new Thread(new ClientHandler(sock, this, useSerialization)).start();
            }
        } catch (IOException e) {
            logger.error("Server error", e);
        }
    }

    public synchronized boolean registerClient(String sessionId, ClientHandler handler) {
        if (clients.containsKey(sessionId)) return false;
        clients.put(sessionId, handler);
        broadcastUserLogin(handler.getUserName());
        return true;
    }

    public synchronized void removeClient(String sessionId, String userName) {
        clients.remove(sessionId);
        broadcastUserLogout(userName);
    }

    public synchronized List<String> getUserList() {
        List<String> list = new ArrayList<>();
        for (ClientHandler c : clients.values()) list.add(c.getUserName());
        return list;
    }

    public synchronized List<ChatMessage> getHistory() {
        return new ArrayList<>(history);
    }

    // --- broadcast methods ---

    public synchronized void broadcastMessage(String from, String msg) {
        // сохраняем в историю
        history.add(new ChatMessage(from, msg, Instant.now()));

        for (ClientHandler c : clients.values()) {
            if (useSerialization) {
                // для сериализованных клиентов: отправляем Event
                c.sendChatEvent(new MessageEvent(from, msg));
            } else {
                // для XML-клиентов: специальный метод
                c.sendXmlChatMessage(from, msg);
            }
        }
    }

    private synchronized void broadcastUserLogin(String user) {
        for (ClientHandler c : clients.values()) {
            // не шлём событие самому залогинившемуся
            if (user.equals(c.getUserName())) {
                continue;
            }
            if (useSerialization) {
                c.sendChatEvent(new UserLoginEvent(user));
            } else {
                c.sendXmlUserLoginEvent(user);
            }
        }
    }

    private synchronized void broadcastUserLogout(String user) {
        for (ClientHandler c : clients.values()) {
            if (useSerialization) {
                c.sendChatEvent(new UserLogoutEvent(user));
            } else {
                c.sendXmlUserLogoutEvent(user);
            }
        }
    }
}
