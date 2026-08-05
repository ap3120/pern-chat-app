package chatapp.handlers;

import io.javalin.config.JavalinConfig;

import java.sql.Connection;
import java.util.Map;

public class LogoutHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        config.routes.post("/logout", ctx ->
        {
            ctx.req().getSession().invalidate();
            ctx.json(Map.of("msg", "User logged out."));
        });
    }
}
