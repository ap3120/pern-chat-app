package chatapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket routing, ported from the former Tyrus {@code WebSocketServer}. Javalin runs
 * WebSocket callbacks on multiple threads, so the client registry is a {@link ConcurrentHashMap}
 * and removal on close is done safely (the old code mutated a plain HashMap while iterating).
 *
 * Behavior is otherwise unchanged: a client announces itself with {"new_client_id": <userId>},
 * then messages are routed to the claimed receiver and echoed to the sender. Authenticating the
 * socket and persisting messages remain a separate, deferred hardening phase.
 */
public final class WsHub
{
    private WsHub() {}

    private static final Map<String, WsContext> clients = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void onConnect(WsConnectContext ctx)
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
        } else if (message.hasNonNull("content") && message.hasNonNull("receiver_id"))
        {
            String receiverId = message.get("receiver_id").asText();
            WsContext receiver = clients.get(receiverId);
            if (receiver != null)
            {
                sendQuietly(receiver, raw);
            }
            sendQuietly(ctx, raw); // echo to sender
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
