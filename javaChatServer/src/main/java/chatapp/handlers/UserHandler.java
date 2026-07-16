package chatapp.handlers;

import chatapp.Postgres;
import chatapp.models.AddUserRequestBody;
import chatapp.models.UpdateUserRequestBody;
import io.javalin.config.JavalinConfig;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class UserHandler
{

    public static void register(JavalinConfig config, Connection connection)
    {
        /* Returns all users */
        config.routes.get("/users", ctx ->
        {
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
            ctx.json(response);
        });

        config.routes.put("/users/{user_id}", ctx ->
        {
            int user_id = Integer.parseInt(ctx.pathParam("user_id"));
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
            ctx.json(response);
        });

        config.routes.delete("/users/{user_id}", ctx ->
        {
            int user_id = Integer.parseInt(ctx.pathParam("user_id"));
            Map<String, Object> response = Postgres.deleteUser(connection, user_id);
            ctx.json(response);
        });
    }
}
