package chatapp.handlers;

import chatapp.Postgres;
import io.javalin.config.JavalinConfig;

import java.sql.Connection;

public class TestHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        config.routes.post("/test/reset-db", ctx ->
        {
            Postgres.cleanDatabase(connection);
            ctx.status(204);
        });
    }
}
