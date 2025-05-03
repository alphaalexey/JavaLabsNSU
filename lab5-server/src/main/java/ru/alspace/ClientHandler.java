package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.alspace.common.model.command.*;
import ru.alspace.common.model.data.ChatMessage;
import ru.alspace.common.model.event.Event;
import ru.alspace.common.model.response.ErrorResponse;
import ru.alspace.common.model.response.HistoryResponse;
import ru.alspace.common.model.response.SuccessResponse;
import ru.alspace.common.model.response.UserListResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ChatServer server;
    private final boolean useSerialization;

    // XML
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    // Serialization
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;

    private String sessionId;
    private String userName;

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
                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
                processSerialization();
            } else {
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                processXml();
            }
        } catch (IOException e) {
            logger.error("Client error", e);
        } finally {
            cleanup();
        }
    }

    // --- XML mode ---
    private void processXml() {
        try {
            // login
            Document loginDoc = safeReadXmlDocument();
            if (!handleXmlLogin(loginDoc)) return;

            // main loop
            loopXml:
            while (true) {
                Document cmd = safeReadXmlDocument();
                if (cmd == null) break;
                Element root = cmd.getDocumentElement();
                String cn = root.getAttribute("name");
                switch (cn) {
                    case "list" -> sendXmlUserList();
                    case "history" -> sendXmlHistory();
                    case "message" -> {
                        String msg = root.getElementsByTagName("message").item(0).getTextContent();
                        server.broadcastMessage(userName, msg);
                        sendXmlSuccess();
                    }
                    case "logout" -> {
                        sendXmlSuccess();
                        break loopXml;
                    }
                    default -> sendXmlError("Unknown cmd: " + cn);
                }
            }
        } catch (Exception e) {
            logger.error("XML processing error", e);
        }
    }

    private boolean handleXmlLogin(Document doc) throws Exception {
        if (doc == null) return false;
        Element r = doc.getDocumentElement();
        if (!"command".equals(r.getNodeName()) || !"login".equals(r.getAttribute("name"))) {
            sendXmlError("Expected login");
            return false;
        }
        String nm = r.getElementsByTagName("name").item(0).getTextContent().trim();
        if (nm.isEmpty()) {
            sendXmlError("Empty name");
            return false;
        }
        userName = nm;
        sessionId = UUID.randomUUID().toString();
        if (!server.registerClient(sessionId, this)) {
            sendXmlError("Already registered");
            return false;
        }
        sendXmlSuccessWithSession(sessionId);
        return true;
    }

    private Document safeReadXmlDocument() {
        try {
            int len = dataIn.readInt();
            if (len <= 0 || len > 100_000) return null;
            byte[] buf = new byte[len];
            dataIn.readFully(buf);
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return db.parse(new ByteArrayInputStream(buf));
        } catch (Exception e) {
            return null;
        }
    }

    private void sendXmlDocument(Document doc) {
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            tf.transform(new DOMSource(doc), new StreamResult(bout));
            byte[] data = bout.toByteArray();
            dataOut.writeInt(data.length);
            dataOut.write(data);
            dataOut.flush();
        } catch (Exception ignored) {
        }
    }

    private void sendXmlError(String msg) throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.newDocument();
        Element e = d.createElement("error");
        d.appendChild(e);
        Element m = d.createElement("message");
        m.setTextContent(msg);
        e.appendChild(m);
        sendXmlDocument(d);
    }

    private void sendXmlSuccess() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.newDocument();
        d.appendChild(d.createElement("success"));
        sendXmlDocument(d);
    }

    private void sendXmlSuccessWithSession(String sid) throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.newDocument();
        Element s = d.createElement("success");
        d.appendChild(s);
        Element se = d.createElement("session");
        se.setTextContent(sid);
        s.appendChild(se);
        sendXmlDocument(d);
    }

    private void sendXmlUserList() throws Exception {
        List<String> ul = server.getUserList();
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.newDocument();
        Element s = d.createElement("success");
        d.appendChild(s);
        Element lu = d.createElement("listusers");
        s.appendChild(lu);
        for (String u : ul) {
            Element ue = d.createElement("user");
            Element ne = d.createElement("name");
            ne.setTextContent(u);
            ue.appendChild(ne);
            Element te = d.createElement("type");
            te.setTextContent("SWING_CLIENT");
            ue.appendChild(te);
            lu.appendChild(ue);
        }
        sendXmlDocument(d);
    }

    private void sendXmlHistory() throws Exception {
        List<ChatMessage> hist = server.getHistory();
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = b.newDocument();
        Element ev = d.createElement("event");
        ev.setAttribute("name", "history");
        d.appendChild(ev);
        for (ChatMessage m : hist) {
            Element me = d.createElement("message");
            me.setAttribute("from", m.fromUser());
            me.setTextContent(m.text());
            ev.appendChild(me);
        }
        sendXmlDocument(d);
    }

    public void sendXmlChatMessage(String from, String msg) {
        try {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = b.newDocument();
            Element ev = d.createElement("event");
            ev.setAttribute("name", "message");
            d.appendChild(ev);
            Element me = d.createElement("message");
            me.setTextContent(msg);
            ev.appendChild(me);
            Element ne = d.createElement("name");
            ne.setTextContent(from);
            ev.appendChild(ne);
            sendXmlDocument(d);
        } catch (Exception ignored) {
        }
    }

    public void sendXmlUserLoginEvent(String user) {
        try {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = b.newDocument();
            Element ev = d.createElement("event");
            ev.setAttribute("name", "userlogin");
            d.appendChild(ev);
            Element ne = d.createElement("name");
            ne.setTextContent(user);
            ev.appendChild(ne);
            sendXmlDocument(d);
        } catch (Exception ignored) {
        }
    }

    public void sendXmlUserLogoutEvent(String user) {
        try {
            DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = b.newDocument();
            Element ev = d.createElement("event");
            ev.setAttribute("name", "userlogout");
            d.appendChild(ev);
            Element ne = d.createElement("name");
            ne.setTextContent(user);
            ev.appendChild(ne);
            sendXmlDocument(d);
        } catch (Exception ignored) {
        }
    }

    // --- Serialization mode ---
    private void processSerialization() {
        try {
            // login
            Object o = objIn.readObject();
            if (!(o instanceof LoginCommand lc)) return;

            userName = lc.userName();
            sessionId = UUID.randomUUID().toString();
            if (!server.registerClient(sessionId, this)) {
                objOut.writeObject(new ErrorResponse("Already registered"));
                return;
            }
            objOut.writeObject(new SuccessResponse(sessionId));

            // main loop
            label:
            while (true) {
                Object cmd = objIn.readObject();
                switch (cmd) {
                    case MessageCommand mc:
                        server.broadcastMessage(userName, mc.text());
                        objOut.writeObject(new SuccessResponse(null));
                        break;
                    case ListCommand ignored:
                        objOut.writeObject(new UserListResponse(server.getUserList()));
                        break;
                    case HistoryCommand ignored:
                        objOut.writeObject(new HistoryResponse(server.getHistory()));
                        break;
                    case LogoutCommand ignored:
                        objOut.writeObject(new SuccessResponse(null));
                        break label;
                    case null:
                    default:
                        objOut.writeObject(new ErrorResponse("Unknown cmd"));
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Serialization processing error", e);
        }
    }

    // отправка событий (MessageEvent, UserLoginEvent, UserLogoutEvent)
    public void sendChatEvent(Event ev) {
        if (!useSerialization) return;
        try {
            objOut.writeObject(ev);
            objOut.flush();
        } catch (IOException ignored) {
        }
    }

    private void cleanup() {
        try {
            if (!useSerialization && sessionId != null) {
                server.removeClient(sessionId, userName);
            }
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
