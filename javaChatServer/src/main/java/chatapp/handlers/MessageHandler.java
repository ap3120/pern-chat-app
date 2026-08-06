package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.AddMessageRequestBody;
import chatapp.utils.Utils;
import io.javalin.config.JavalinConfig;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static chatapp.utils.Utils.requireLogin;

public class MessageHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        /* Returns all the messages from a chat */
        config.routes.get("/message/{chat_id}", ctx ->
        {
            requireLogin(ctx);
            int chat_id = Integer.parseInt(ctx.pathParam("chat_id"));
            List<Map<String, Object>> response = Postgres.getMessagesFromChat(connection, chat_id);
            ctx.json(response);
        });

        /* Writes a new message */
        config.routes.post("/message", ctx ->
        {
            requireLogin(ctx);
            AddMessageRequestBody body = ctx.bodyValidator(AddMessageRequestBody.class)
                    .check(b -> Utils.isInt(b.sender_id), "sender_id must be an integer.")
                    .check(b -> b.sender_username != null, "sender_username is required.")
                    .check(b -> Utils.isInt(b.receiver_id), "receiver_id must be an integer.")
                    .check(b -> b.receiver_username != null, "receiver_username is required.")
                    .check(b -> Utils.isInt(b.chat_id), "chat_id must be an integer.")
                    .get();
            Map<String, Object> response = Postgres.addMessage(
                    connection,
                    body.content,
                    Integer.parseInt(body.sender_id),
                    body.sender_username,
                    Integer.parseInt(body.receiver_id),
                    body.receiver_username,
                    Integer.parseInt(body.chat_id)
            );
            ctx.json(response);
        });
    }
}
