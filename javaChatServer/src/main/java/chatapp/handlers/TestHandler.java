package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.AddUserRequestBody;
import io.javalin.config.JavalinConfig;

import java.sql.Connection;
import java.util.Map;

public class TestHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        config.routes.post("/test/reset-db", ctx ->
        {
            Postgres.cleanDatabase(connection);
            ctx.status(204);
        });

        config.routes.post("/test/create-user", ctx ->
        {
            AddUserRequestBody body = ctx.bodyValidator(AddUserRequestBody.class)
                    .check(b -> b.username != null, "username is required")
                    .check(b -> b.password != null, "password is required.")
                    .get();
            Map<String, Object> response = Postgres.addUser(connection, body.username, body.password);
            ctx.json(response);
        });
    }
}
