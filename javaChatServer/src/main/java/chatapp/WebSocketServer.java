package chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws")
public class WebSocketServer {
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> clients = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("New connection established: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("Received message: " + message);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonMessage = mapper.readTree(message);
        if (jsonMessage.get("new_client_id") != null) {
            clients.put(jsonMessage.get("new_client_id").asText(), session.getId());
        } else if (jsonMessage.get("content") != null && jsonMessage.get("receiver_id") != null) {
            String receiverId = jsonMessage.get("receiver_id").asText();
            for (Session s : sessions) {
                if (clients.get(receiverId) != null && clients.get(receiverId).equals(s.getId())) {
                    s.getBasicRemote().sendText(message);
                }
            }
            session.getBasicRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        for (String key : clients.keySet()) {
            System.out.println(key);
            if (clients.get(key).equals(session.getId())) {
                System.out.println("Removing this user: " + key);
                clients.remove(key);
            }
        }
        System.out.println("Connection closed: " + session.getId());
    }
}
