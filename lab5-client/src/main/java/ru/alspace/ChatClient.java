package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.alspace.common.model.command.HistoryCommand;
import ru.alspace.common.model.command.ListCommand;
import ru.alspace.common.model.command.LoginCommand;
import ru.alspace.common.model.command.MessageCommand;
import ru.alspace.common.model.data.ChatMessage;
import ru.alspace.common.model.event.MessageEvent;
import ru.alspace.common.model.event.UserLoginEvent;
import ru.alspace.common.model.event.UserLogoutEvent;
import ru.alspace.common.model.response.ErrorResponse;
import ru.alspace.common.model.response.HistoryResponse;
import ru.alspace.common.model.response.SuccessResponse;
import ru.alspace.common.model.response.UserListResponse;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatClient extends JFrame {
    private static final String CLIENT_ID = "SWING_CLIENT";
    private static final Logger logger = LogManager.getLogger(ChatClient.class);

    private final String userName;
    private final boolean useSerialization;
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private Socket socket;
    // XML streams
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    // Serialization streams
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;
    private String sessionId;

    public ChatClient(String host, int port, String userName, boolean useSerialization) {
        this.userName = userName;
        this.useSerialization = useSerialization;

        setTitle("Чат - " + userName + (useSerialization ? " [SER]" : " [XML]"));
        setSize(600, 400);
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();

        try {
            socket = new Socket(host, port);
            if (useSerialization) {
                objOut = new ObjectOutputStream(socket.getOutputStream());
                try {
                    objIn = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    showProtocolError("Сервер работает на XML-протоколе. Перезапустите с XML.");
                    return;
                }
            } else {
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());
            }
            sendLogin();
            new Thread(this::incomingLoop).start();
            inputField.requestFocusInWindow();
        } catch (Exception e) {
            logger.error("Connect error", e);
            JOptionPane.showMessageDialog(this, "Cannot connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initUI() {
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(120, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userScroll, chatScroll);
        split.setDividerLocation(120);

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener((ActionEvent e) -> sendMessage());
        getRootPane().setDefaultButton(sendBtn);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);

        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    private void showProtocolError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Protocol mismatch", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void sendLogin() {
        if (useSerialization) {
            try {
                objOut.writeObject(new LoginCommand(userName, CLIENT_ID));
                objOut.flush();
                Object resp = objIn.readObject();
                if (resp instanceof SuccessResponse(String id)) {
                    sessionId = id;
                } else if (resp instanceof ErrorResponse(String message)) {
                    showProtocolError(message);
                }
            } catch (Exception ex) {
                showProtocolError("Сервер ожидает XML-протокол. Перезапустите клиент с XML.");
            }
        } else {
            try {
                sendCommand("login", Map.of("name", userName, "type", CLIENT_ID));
                Document d = readXml();
                if (d == null) throw new IOException();
                sessionId = d.getElementsByTagName("session").item(0).getTextContent();
            } catch (Exception ex) {
                showProtocolError("Сервер ожидает сериализацию. Перезапустите клиент с Serialization.");
            }
        }
        if (sessionId != null) {
            sendList();
            sendHistory();
        }
    }

    private void sendList() {
        try {
            if (useSerialization) {
                objOut.writeObject(new ListCommand(sessionId));
                objOut.flush();
                Object resp = objIn.readObject();
                if (resp instanceof UserListResponse(List<String> users)) {
                    // прямо обновляем список всех юзеров
                    updateUserList(users);
                }
            } else {
                sendCommand("list", Map.of("session", sessionId));
                Document doc = readXml();
                if (doc == null) return;

                // собираем ВСЕ имена в один список
                List<String> users = new ArrayList<>();
                NodeList userElems = doc.getElementsByTagName("user");
                for (int i = 0; i < userElems.getLength(); i++) {
                    Element ue = (Element) userElems.item(i);
                    String name = ue.getElementsByTagName("name").item(0).getTextContent().trim();
                    users.add(name);
                }
                // один вызов — одно полное обновление модели
                updateUserList(users);
            }
        } catch (Exception e) {
            logger.error("List error", e);
        }
    }

    private void sendHistory() {
        try {
            if (useSerialization) {
                objOut.writeObject(new HistoryCommand(sessionId));
                objOut.flush();
                Object r = objIn.readObject();
                if (r instanceof HistoryResponse(List<ChatMessage> history)) {
                    history.forEach(m -> appendChat(m.fromUser() + ": " + m.text()));
                }
            } else {
                sendCommand("history", Map.of("session", sessionId));
                Document d = readXml();
                assert d != null;
                NodeList nm = d.getElementsByTagName("message");
                for (int i = 0; i < nm.getLength(); i++) {
                    Element me = (Element) nm.item(i);
                    String from = me.getAttribute("from");
                    String txt = me.getTextContent();
                    appendChat(from + ": " + txt);
                }
            }
        } catch (Exception e) {
            logger.error("History error", e);
        }
    }

    private void sendMessage() {
        String txt = inputField.getText().trim();
        if (txt.isEmpty()) return;
        inputField.setText("");
        try {
            if (useSerialization) {
                objOut.writeObject(new MessageCommand(sessionId, txt));
                objOut.flush();
            } else {
                sendCommand("message", Map.of("session", sessionId, "message", txt));
            }
        } catch (Exception e) {
            logger.error("Send msg error", e);
        }
    }

    private void incomingLoop() {
        try {
            while (true) {
                if (useSerialization) {
                    Object o = objIn.readObject();
                    if (o instanceof MessageEvent(String fromUser, String text)) {
                        appendChat(fromUser + ": " + text);
                    } else if (o instanceof UserLoginEvent(String name)) {
                        // добавляем в список и печатаем в чат
                        addNewUserToUserList(name);
                        appendChat(name + " вошёл в чат");
                    } else if (o instanceof UserLogoutEvent(String name)) {
                        removeUserFromUserList(name);
                        appendChat(name + " покинул чат");
                    }
                } else {
                    Document d = readXml();
                    if (d == null) continue;
                    Element r = d.getDocumentElement();
                    if (!"event".equals(r.getNodeName())) continue;
                    switch (r.getAttribute("name")) {
                        case "message" -> {
                            String msg = r.getElementsByTagName("message").item(0).getTextContent();
                            String from = r.getElementsByTagName("name").item(0).getTextContent();
                            appendChat(from + ": " + msg);
                        }
                        case "userlogin" -> {
                            String u = r.getElementsByTagName("name").item(0).getTextContent();
                            addNewUserToUserList(u);
                            appendChat(u + " вошёл в чат");
                        }
                        case "userlogout" -> {
                            String u = r.getElementsByTagName("name").item(0).getTextContent();
                            removeUserFromUserList(u);
                            appendChat(u + " покинул чат");
                        }
                        case "history" -> {
                            NodeList msgs = r.getElementsByTagName("message");
                            for (int i = 0; i < msgs.getLength(); i++) {
                                Element me = (Element) msgs.item(i);
                                appendChat(me.getAttribute("from") + ": " + me.getTextContent());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Incoming loop error", e);
        }
    }

    // XML helpers
    private void sendXmlDocument(Document doc) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        tf.transform(new DOMSource(doc), new StreamResult(bout));
        byte[] data = bout.toByteArray();
        dataOut.writeInt(data.length);
        dataOut.write(data);
        dataOut.flush();
    }

    private void sendCommand(String name, Map<String, String> elements) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Element cmd = doc.createElement("command");
        cmd.setAttribute("name", name);
        doc.appendChild(cmd);
        for (var e : elements.entrySet()) {
            Element el = doc.createElement(e.getKey());
            el.setTextContent(e.getValue());
            cmd.appendChild(el);
        }
        sendXmlDocument(doc);
    }

    private Document readXml() {
        try {
            int len = dataIn.readInt();
            byte[] buf = new byte[len];
            dataIn.readFully(buf);
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(new ByteArrayInputStream(buf));
        } catch (Exception e) {
            return null;
        }
    }

    // UI updates
    private void appendChat(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            users.forEach(userListModel::addElement);
        });
    }

    private void addNewUserToUserList(String user) {
        SwingUtilities.invokeLater(() -> {
            if (!userListModel.contains(user)) {
                userListModel.addElement(user);
            }
        });
    }

    private void removeUserFromUserList(String user) {
        SwingUtilities.invokeLater(() -> userListModel.removeElement(user));
    }
}
