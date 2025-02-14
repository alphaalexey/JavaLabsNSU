package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient extends JFrame {
    private static final Logger logger = LogManager.getLogger(ChatClient.class);

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private String sessionId;
    private final String userName;

    public ChatClient(String host, int port, String userName) {
        this.userName = userName;
        setTitle("Чат клиент - " + userName);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();

        try {
            socket = new Socket(host, port);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            sendLogin(); // Отправляем команду логина
            new Thread(new IncomingReader()).start(); // Поток для чтения сообщений
        } catch (IOException e) {
            logger.error("Ошибка подключения к серверу", e);
            JOptionPane.showMessageDialog(this, "Ошибка подключения к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();
        sendButton = new JButton("Отправить");

        sendButton.addActionListener((ActionEvent e) -> sendMessage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        getContentPane().add(panel, BorderLayout.SOUTH);
    }

    private void sendLogin() {
        try {
            String xml = "<command name=\"login\">" +
                    "<name>" + escapeXml(userName) + "</name>" +
                    "<type>SWING_CLIENT</type>" +
                    "</command>";
            sendXmlMessage(xml);
        } catch (Exception e) {
            logger.error("Ошибка отправки команды логина", e);
        }
    }

    private void sendMessage() {
        try {
            String text = inputField.getText().trim();
            if (text.isEmpty()) return;
            String xml = "<command name=\"message\">" +
                    "<message>" + escapeXml(text) + "</message>" +
                    "<session>" + escapeXml(sessionId) + "</session>" +
                    "</command>";
            sendXmlMessage(xml);
            inputField.setText("");
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }

    // Простейшая функция экранирования спецсимволов XML
    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // Отправка XML-сообщения с префиксом длины
    private void sendXmlMessage(String xmlString) throws Exception {
        byte[] data = xmlString.getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(data.length);
        dataOut.write(data);
        dataOut.flush();
    }

    // Чтение XML-документа от сервера с обработкой ошибок
    private Document safeReadXmlDocument() {
        try {
            int length = dataIn.readInt();
            if (length <= 0 || length > 10_000) {
                logger.warn("Получена некорректная длина сообщения: " + length);
                return null;
            }
            byte[] data = new byte[length];
            dataIn.readFully(data);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse(byteIn);
        } catch (Exception e) {
            logger.error("Ошибка чтения XML документа", e);
            return null;
        }
    }

    // Поток для чтения входящих сообщений
    private class IncomingReader implements Runnable {
        public void run() {
            try {
                while (true) {
                    Document doc = safeReadXmlDocument();
                    if (doc == null) continue;
                    processXmlDocument(doc);
                }
            } catch (Exception e) {
                logger.error("Ошибка чтения сообщений", e);
            }
        }
    }

    // Обработка полученного XML-сообщения
    private void processXmlDocument(Document doc) {
        try {
            Element root = doc.getDocumentElement();
            if (root == null) return;
            String nodeName = root.getNodeName();
            switch (nodeName) {
                case "success" -> {
                    NodeList sessions = root.getElementsByTagName("session");
                    if (sessions.getLength() > 0) {
                        sessionId = sessions.item(0).getTextContent().trim();
                        appendChat("Успешный вход. Сессия: " + sessionId);
                    }
                }
                case "error" -> {
                    NodeList messages = root.getElementsByTagName("message");
                    if (messages.getLength() > 0) {
                        String errorMsg = messages.item(0).getTextContent().trim();
                        appendChat("Ошибка: " + errorMsg);
                    }
                }
                case "event" -> {
                    String eventName = root.getAttribute("name");
                    switch (eventName) {
                        case "message" -> {
                            NodeList messageNodes = root.getElementsByTagName("message");
                            NodeList nameNodes = root.getElementsByTagName("name");
                            if (messageNodes.getLength() > 0 && nameNodes.getLength() > 0) {
                                String msg = messageNodes.item(0).getTextContent().trim();
                                String fromUser = nameNodes.item(0).getTextContent().trim();
                                appendChat(fromUser + ": " + msg);
                            }
                        }
                        case "userlogin" -> {
                            NodeList nameNodes = root.getElementsByTagName("name");
                            if (nameNodes.getLength() > 0) {
                                String newUser = nameNodes.item(0).getTextContent().trim();
                                appendChat("Пользователь " + newUser + " вошел в чат");
                            }
                        }
                        case "userlogout" -> {
                            NodeList nameNodes = root.getElementsByTagName("name");
                            if (nameNodes.getLength() > 0) {
                                String user = nameNodes.item(0).getTextContent().trim();
                                appendChat("Пользователь " + user + " покинул чат");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка обработки входящего XML сообщения", e);
        }
    }

    private void appendChat(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }
}
