package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ChatServer server;
    private final boolean useSerialization;
    private String sessionId;
    private String userName;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    public ClientHandler(Socket socket, ChatServer server, boolean useSerialization) {
        this.socket = socket;
        this.server = server;
        this.useSerialization = useSerialization;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public void run() {
        try {
            if (useSerialization) {
                // Реализация на базе сериализации
                processSerialization();
            } else {
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                processXml();
            }
        } catch (IOException e) {
            logger.error("Ошибка в работе клиента", e);
        } finally {
            cleanup();
        }
    }

    private void processSerialization() {
        // Реализацию можно выполнить аналогично XML-версии, используя объекты.
        // TODO: implement
    }

    private void processXml() {
        try {
            // Пытаемся прочитать и обработать команду login
            Document loginDoc = safeReadXmlDocument();
            if (loginDoc == null) {
                return; // если не удалось прочитать, завершаем обработку
            }

            Element root = loginDoc.getDocumentElement();
            if (root == null || !"command".equals(root.getNodeName()) || !"login".equals(root.getAttribute("name"))) {
                sendXmlError("Ожидалась команда login");
                return;
            }

            // Читаем имя пользователя
            NodeList nameNodes = root.getElementsByTagName("name");
            if (nameNodes.getLength() == 0 || nameNodes.item(0).getTextContent().trim().isEmpty()) {
                sendXmlError("Не указано имя пользователя");
                return;
            }
            userName = nameNodes.item(0).getTextContent().trim();

            // Генерируем уникальный sessionId и регистрируем клиента
            sessionId = UUID.randomUUID().toString();
            if (!server.registerClient(sessionId, this)) {
                sendXmlError("Пользователь уже зарегистрирован");
                return;
            }
            sendXmlSuccessWithSession(sessionId);
            logger.info("Пользователь {} вошел в чат с сессией {}", userName, sessionId);

            // Основной цикл обработки команд клиента
            label:
            while (true) {
                Document doc = safeReadXmlDocument();
                if (doc == null) {
                    break; // если не удалось прочитать документ, завершаем сессию
                }

                try {
                    Element cmd = doc.getDocumentElement();
                    if (cmd == null) {
                        sendXmlError("Пустой запрос");
                        continue;
                    }
                    String commandName = cmd.getAttribute("name");
                    switch (commandName) {
                        case "list":
                            sendUserList();
                            break;
                        case "message":
                            NodeList messageNodes = cmd.getElementsByTagName("message");
                            if (messageNodes.getLength() == 0 || messageNodes.item(0).getTextContent().trim().isEmpty()) {
                                sendXmlError("Пустое сообщение");
                                continue;
                            }
                            String message = messageNodes.item(0).getTextContent().trim();
                            server.broadcastMessage(message, userName);
                            sendXmlSuccess();
                            break;
                        case "logout":
                            sendXmlSuccess();
                            break label;
                        default:
                            sendXmlError("Неизвестная команда: " + commandName);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Ошибка обработки запроса от пользователя {}", userName, e);
                    sendXmlError("Внутренняя ошибка сервера");
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка обработки XML сообщений", e);
        }
    }

    // Чтение XML-документа с защитой от ошибок
    private Document safeReadXmlDocument() {
        try {
            int length = dataIn.readInt();
            if (length <= 0 || length > 10_000) { // ограничиваем максимальную длину
                logger.warn("Получена некорректная длина сообщения: {}", length);
                return null;
            }
            byte[] data = new byte[length];
            dataIn.readFully(data);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(byteIn);
        } catch (EOFException e) {
            logger.info("Сессия закончена {} {}", userName, sessionId);
            return null;
        } catch (Exception e) {
            logger.error("Ошибка чтения XML документа", e);
            return null;
        }
    }

    // Отправка XML-документа клиенту
    private void sendXmlDocument(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(byteOut));
            byte[] data = byteOut.toByteArray();
            dataOut.writeInt(data.length);
            dataOut.write(data);
            dataOut.flush();
        } catch (Exception e) {
            logger.error("Ошибка отправки XML документа", e);
        }
    }

    private void sendXmlError(String message) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element error = doc.createElement("error");
            doc.appendChild(error);
            Element msg = doc.createElement("message");
            msg.setTextContent(message);
            error.appendChild(msg);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки XML ошибки", e);
        }
    }

    private void sendXmlSuccess() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element success = doc.createElement("success");
            doc.appendChild(success);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки XML успеха", e);
        }
    }

    private void sendXmlSuccessWithSession(String sessionId) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element success = doc.createElement("success");
            doc.appendChild(success);
            Element sessionElem = doc.createElement("session");
            sessionElem.setTextContent(sessionId);
            success.appendChild(sessionElem);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки XML успеха с сессией", e);
        }
    }

    // Отправка списка пользователей
    private void sendUserList() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element success = doc.createElement("success");
            doc.appendChild(success);
            Element listUsers = doc.createElement("listusers");
            success.appendChild(listUsers);
            for (String user : server.getUserList()) {
                Element userElem = doc.createElement("user");
                Element nameElem = doc.createElement("name");
                nameElem.setTextContent(user);
                userElem.appendChild(nameElem);
                Element typeElem = doc.createElement("type");
                typeElem.setTextContent("SWING_CLIENT"); // для примера
                userElem.appendChild(typeElem);
                listUsers.appendChild(userElem);
            }
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки списка пользователей", e);
        }
    }

    // Отправка сообщения чата (событие от сервера)
    public void sendChatMessage(String fromUser, String message) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element event = doc.createElement("event");
            event.setAttribute("name", "message");
            doc.appendChild(event);
            Element messageElem = doc.createElement("message");
            messageElem.setTextContent(message);
            event.appendChild(messageElem);
            Element nameElem = doc.createElement("name");
            nameElem.setTextContent(fromUser);
            event.appendChild(nameElem);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }

    // Отправка событий подключения/отключения пользователя
    public void sendUserLoginEvent(String newUser) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element event = doc.createElement("event");
            event.setAttribute("name", "userlogin");
            doc.appendChild(event);
            Element nameElem = doc.createElement("name");
            nameElem.setTextContent(newUser);
            event.appendChild(nameElem);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки события входа пользователя", e);
        }
    }

    public void sendUserLogoutEvent(String user) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element event = doc.createElement("event");
            event.setAttribute("name", "userlogout");
            doc.appendChild(event);
            Element nameElem = doc.createElement("name");
            nameElem.setTextContent(user);
            event.appendChild(nameElem);
            sendXmlDocument(doc);
        } catch (Exception e) {
            logger.error("Ошибка отправки события выхода пользователя", e);
        }
    }

    private void cleanup() {
        try {
            if (!useSerialization && sessionId != null) {
                server.removeClient(sessionId, userName);
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.error("Ошибка закрытия соединения", e);
        }
    }
}
