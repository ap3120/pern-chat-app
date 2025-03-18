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
        } else if (jsonMessage.get("client_id_to_remove") != null) {
            clients.remove(jsonMessage.get("client_id_to_remove").asText());
        } else if (jsonMessage.get("content") != null && jsonMessage.get("sender_id") != null && jsonMessage.get("receiver_id") != null) {
            for (Session s : sessions) {
                if (clients.get(jsonMessage.get("receiver_id").asText()).equals(s.getId())) {
                    s.getBasicRemote().sendText(message);
                }
            }
            session.getBasicRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Connection closed: " + session.getId());
    }
}
