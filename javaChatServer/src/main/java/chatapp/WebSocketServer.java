package chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServer
{
    private static final Map<String, WsContext> clients = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void onOpen(WsConnectContext ctx)
    {
        System.out.println("New connection established: " + ctx.sessionId());
    }

    public static void onMessage(WsMessageContext ctx)
    {

        String raw = ctx.message();
        JsonNode message;
        try
        {
            message = mapper.readTree(raw);
        } catch (Exception e)
        {
            System.out.println("Ignoring malformed websocket frame: " + e.getMessage());
            return;
        }

        if (message.hasNonNull("new_client_id"))
        {
            clients.put(message.get("new_client_id").asText(), ctx);
        } else if (message.has("type") && message.get("type").asText().equals("ping"))
        {
            ctx.send("{\"type\":\"pong\"}");
        } else if (message.hasNonNull("content") && message.hasNonNull("receiver_id"))
        {
            String receiverId = message.get("receiver_id").asText();
            WsContext receiver = clients.get(receiverId);
            if (receiver != null)
            {
                sendQuietly(receiver, raw);
            }
            sendQuietly(ctx, raw);
        }
    }

    public static void onClose(WsCloseContext ctx)
    {
        clients.values().removeIf(c -> c.sessionId().equals(ctx.sessionId()));
        System.out.println("Connection closed: " + ctx.sessionId());
    }

    private static void sendQuietly(WsContext ctx, String message)
    {
        try
        {
            ctx.send(message);
        } catch (Exception e)
        {
            // Receiver may have disconnected between lookup and send; drop the stale entry.
            clients.values().removeIf(c -> c.sessionId().equals(ctx.sessionId()));
        }
    }
}
