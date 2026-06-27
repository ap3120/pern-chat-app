package chatapp.controllers;

import chatapp.Postgres;
import chatapp.dto.AddUserChatRequest;
import chatapp.dto.CreateChatRequest;
import io.javalin.config.JavalinConfig;

import javax.sql.DataSource;
import java.sql.Connection;

public class ChatController
{
    public static void register(JavalinConfig config, DataSource ds)
    {
        config.routes.post("/chat", ctx ->
        {
            CreateChatRequest body = ctx.bodyValidator(CreateChatRequest.class)
                    .check(b -> isInt(b.created_by), "created_by must be an integer")
                    .get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.addToChat(c, Integer.parseInt(body.created_by)));
            }
        });

        // Literal routes registered before the /chat/{userId} param route (Javalin matches in
        // definition order).
        config.routes.post("/chat/users_chats", ctx ->
        {
            AddUserChatRequest body = ctx.bodyValidator(AddUserChatRequest.class)
                    .check(b -> b.user_id != null, "user_id is required")
                    .check(b -> b.chat_id != null, "chat_id is required")
                    .get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.addToUsersChats(c, body.user_id, body.chat_id));
            }
        });

        config.routes.get("/chat/users_chats/{chatId}/{userId}", ctx ->
        {
            int chatId = ctx.pathParamAsClass("chatId", Integer.class).get();
            int userId = ctx.pathParamAsClass("userId", Integer.class).get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.getUser(c, chatId, userId));
            }
        });

        config.routes.get("/chat/{userId}", ctx ->
        {
            int userId = ctx.pathParamAsClass("userId", Integer.class).get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.readFromChats(c, userId));
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
