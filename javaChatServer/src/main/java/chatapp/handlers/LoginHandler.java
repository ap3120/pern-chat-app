package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.LoginRequestBody;
import io.javalin.config.JavalinConfig;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class LoginHandler
{
    public static void register(JavalinConfig config, Connection connection) throws IOException
    {
        config.routes.post("/login", ctx ->
        {
            LoginRequestBody body = ctx.bodyValidator(LoginRequestBody.class)
                    .check(b -> b.username != null, "username is required.")
                    .check(b -> b.password != null, "password is required.")
                    .get();
            Map<String, Object> response = Postgres.login(connection, body.username, body.password);
            if (response.containsKey("user_id"))
            {
                int userId = (Integer) response.get("user_id");
                // Create server session
                ctx.sessionAttribute("user_id", userId);
            }
            ctx.json(response);
        });
    }
}
