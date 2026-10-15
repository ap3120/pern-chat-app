package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.CreateChatRequestBody;
import chatapp.utils.Utils;
import io.javalin.config.JavalinConfig;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static chatapp.utils.Utils.requireLogin;

public class ChatHandler
{
    public static void register(JavalinConfig config, Connection connection) throws IOException
    {
        /* Add a new chat */
        config.routes.post("/chat", ctx ->
        {
            requireLogin(ctx);
            CreateChatRequestBody body = ctx.bodyValidator(CreateChatRequestBody.class)
                    .check(b -> Utils.isInt(b.created_by), "created_by must be an integer")
                    .check(b -> Utils.isInt(b.other_user_id), "other_user_id must be an integer")
                    .get();
            Map<String, Object> response = Postgres.createChat(connection, Integer.parseInt(body.created_by),
                    Integer.parseInt(body.other_user_id));
            if (response.containsKey("error"))
            {
                ctx.status(409);
            }
            ctx.json(response);
        });

        /* Returns all the chats of a user*/
        config.routes.get("/chat", ctx ->
        {
            int user_id = requireLogin(ctx);
            List<Map<String, Object>> response = Postgres.getChatsFromUser(connection, user_id);
            ctx.json(response);
        });
    }
}
