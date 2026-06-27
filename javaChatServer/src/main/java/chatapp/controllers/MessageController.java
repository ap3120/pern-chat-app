package chatapp.controllers;

import chatapp.Postgres;
import chatapp.dto.CreateMessageRequest;
import io.javalin.config.JavalinConfig;

import javax.sql.DataSource;
import java.sql.Connection;

public class MessageController
{
    public static void register(JavalinConfig config, DataSource ds)
    {
        config.routes.post("/message", ctx ->
        {
            CreateMessageRequest body = ctx.bodyValidator(CreateMessageRequest.class)
                    .check(b -> b.content != null, "content is required")
                    .check(b -> isInt(b.sender_id), "sender_id must be an integer")
                    .check(b -> b.receiver_id != null, "receiver_id is required")
                    .check(b -> b.chat_id != null, "chat_id is required")
                    .get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.addMessage(c,
                        body.content,
                        Integer.parseInt(body.sender_id),
                        body.sender_username,
                        body.receiver_id,
                        body.receiver_username,
                        body.chat_id));
            }
        });

        config.routes.get("/message/{chatId}", ctx ->
        {
            int chatId = ctx.pathParamAsClass("chatId", Integer.class).get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.getMessagesFromChat(c, chatId));
            }
        });
    }

    private static boolean isInt(String s)
    {
        if (s == null) return false;
        try
        {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }
}
