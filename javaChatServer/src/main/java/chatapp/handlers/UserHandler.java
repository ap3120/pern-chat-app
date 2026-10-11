package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.AddUserRequestBody;
import chatapp.models.UpdateUserRequestBody;
import io.javalin.config.JavalinConfig;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static chatapp.utils.Utils.requireLogin;

public class UserHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        /* Returns all users */
        config.routes.get("/users", ctx ->
        {
            requireLogin(ctx);
            List<Map<String, Object>> response = Postgres.getUsers(connection);
            ctx.json(response);
        });

        /* Adds a user*/
        config.routes.post("/users", ctx ->
        {
            AddUserRequestBody body = ctx.bodyValidator(AddUserRequestBody.class)
                    .check(b -> b.username != null, "username is required")
                    .check(b -> b.password != null, "password is required.")
                    .get();
            Map<String, Object> response = Postgres.addUser(connection, body.username, body.password);
            if (response.containsKey("error"))
            {
                ctx.status(409);
            }
            ctx.json(response);
        });

        config.routes.put("/users", ctx ->
        {
            int user_id = requireLogin(ctx);
            UpdateUserRequestBody body = ctx.bodyValidator(UpdateUserRequestBody.class)
                    .check(b -> b.currentPassword != null, "currentPassword is required.")
                    .check(b -> b.password != null, "password is required.")
                    .get();
            Map<String, Object> response = Postgres.updateUser(
                    connection,
                    user_id,
                    body.currentPassword,
                    body.password
            );
            if (response.containsKey("error"))
            {
                ctx.status(409);
            }
            ctx.json(response);
        });

        config.routes.delete("/users", ctx ->
        {
            int user_id = requireLogin(ctx);
            Map<String, Object> response = Postgres.deleteUser(connection, user_id);
            if (response.containsKey("error"))
            {
                ctx.status(404);
            }
            ctx.json(response);
        });
    }
}
