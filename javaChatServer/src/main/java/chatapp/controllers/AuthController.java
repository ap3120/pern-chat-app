package chatapp.controllers;

import chatapp.Postgres;
import chatapp.dto.RegisterRequest;
import io.javalin.config.JavalinConfig;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class AuthController
{
    public static void register(JavalinConfig config, DataSource ds)
    {
        config.routes.post("/register", ctx ->
        {
            RegisterRequest body = ctx.bodyValidator(RegisterRequest.class)
                    .check(b -> b.username != null && !b.username.isBlank(), "username is required")
                    .check(b -> b.password != null && !b.password.isEmpty(), "password is required")
                    .get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.addUser(c, body.username, body.password));
            }
        });

        config.routes.post("/login", ctx ->
        {
            RegisterRequest body = ctx.bodyValidator(RegisterRequest.class)
                    .check(b -> b.username != null && !b.username.isBlank(), "username is required")
                    .check(b -> b.password != null && !b.password.isEmpty(), "password is required")
                    .get();
            try (Connection c = ds.getConnection())
            {
                JSONObject result = Postgres.login(c, body.username, body.password);
                if (result == null)
                {
                    Responses.serverError(ctx);
                    return;
                }
                // Only mint a session cookie on a successful login (a failure returns {"msg": ...}).
                if (result.has("user_id"))
                {
                    String username = URLEncoder.encode(body.username, StandardCharsets.UTF_8);
                    ctx.header("Set-Cookie",
                            "session_id=" + username + "; Path=/; Max-Age=3600; HttpOnly; SameSite=Lax");
                }
                Responses.ok(ctx, result);
            }
        });

        // Logout has no server-side session to clear yet (real sessions are a deferred phase);
        // the frontend clears its own sessionStorage. Respond 200 so the route is not a 404.
        config.routes.post("/logout", ctx -> ctx.status(200).contentType(Responses.JSON).result("{}"));
    }
}
