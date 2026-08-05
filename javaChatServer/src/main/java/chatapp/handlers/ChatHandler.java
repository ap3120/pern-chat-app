package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.AddUserToChatRequestBody;
import chatapp.models.CreateChatRequestBody;
import chatapp.utils.Utils;
import io.javalin.config.JavalinConfig;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class ChatHandler
{
    public static void register(JavalinConfig config, Connection connection) throws IOException
    {
        /* Add a new chat */
        config.routes.post("/chat", ctx ->
        {
            CreateChatRequestBody body = ctx.bodyValidator(CreateChatRequestBody.class)
                    .check(b -> Utils.isInt(b.created_by),
                            "created_by must be an integer")
                    .get();
            Map<String, Object> response = Postgres.createChat(connection, Integer.parseInt(body.created_by));
            ctx.json(response);
        });

        /* Add a participant to a chat */
        config.routes.post("/chat/users_chats", ctx ->
        {
            AddUserToChatRequestBody body = ctx.bodyValidator(AddUserToChatRequestBody.class)
                    .check(b -> Utils.isInt(b.user_id), "user_id must be an integer")
                    .check(b -> Utils.isInt(b.chat_id), "chat_id must be an integer")
                    .get();
            Map<String, Object> response = Postgres.addParticipantToChat(connection, Integer.parseInt(body.user_id),
                    Integer.parseInt(body.chat_id));
            ctx.json(response);
        });

        /* Returns all the chats of a user*/
        config.routes.get("/chat", ctx ->
        {
            int user_id = Utils.requireLogin(ctx);
            List<Map<String, Object>> response = Postgres.getChatsFromUser(connection, user_id);
            ctx.json(response);
        });
    }
}
