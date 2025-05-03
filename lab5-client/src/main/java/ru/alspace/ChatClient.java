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
import ru.alspace.common.model.response.HistoryResponse;
import ru.alspace.common.model.response.SuccessResponse;
import ru.alspace.common.model.response.UserListResponse;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends JFrame {
    private static final Logger logger = LogManager.getLogger(ChatClient.class);

    private final String userName;
    private final boolean useSerialization;
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private Socket socket;
    // XML
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    // Serialization
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
                objIn = new ObjectInputStream(socket.getInputStream());
            } else {
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());
            }
            sendLogin();
            new Thread(this::incomingLoop).start();
            inputField.requestFocusInWindow();
        } catch (IOException e) {
            logger.error("Connect error", e);
            JOptionPane.showMessageDialog(this, "Cannot connect", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
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

    private void sendLogin() {
        try {
            if (useSerialization) {
                objOut.writeObject(new LoginCommand(userName, "SWING_CLIENT"));
                objOut.flush();
                Object resp = objIn.readObject();
                if (resp instanceof SuccessResponse(String id)) {
                    sessionId = id;
                }
            } else {
                String xml = "<command name=\"login\"><name>" + escapeXml(userName)
                        + "</name><type>SWING_CLIENT</type></command>";
                sendXml(xml);
                Document d = readXml();
                assert d != null;

                sessionId = d.getElementsByTagName("session").item(0).getTextContent();
            }
            // after login request user list & history
            sendList();
            sendHistory();
        } catch (Exception e) {
            logger.error("Login error", e);
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
                // шлём XML-запрос
                String xml = "<command name=\"list\"><session>" + sessionId + "</session></command>";
                sendXml(xml);
                Document doc = readXml();
                if (doc == null) return;

                // собираем ВСЕ имена в один список
                List<String> users = new ArrayList<>();
                NodeList userElems = doc.getElementsByTagName("user");
                for (int i = 0; i < userElems.getLength(); i++) {
                    Element ue = (Element) userElems.item(i);
                    String name = ue.getElementsByTagName("name")
                            .item(0)
                            .getTextContent()
                            .trim();
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
                String xml = "<command name=\"history\"><session>" + sessionId + "</session></command>";
                sendXml(xml);
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
                String xml = "<command name=\"message\"><session>" + sessionId
                        + "</session><message>" + escapeXml(txt)
                        + "</message></command>";
                sendXml(xml);
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
                            userListModel.removeElement(u);
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

    // --- XML helpers ---
    private void sendXml(String xml) throws IOException {
        byte[] data = xml.getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(data.length);
        dataOut.write(data);
        dataOut.flush();
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
