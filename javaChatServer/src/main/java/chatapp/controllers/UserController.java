package chatapp.controllers;

import chatapp.Postgres;
import chatapp.dto.UpdateUserRequest;
import io.javalin.config.JavalinConfig;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserController
{
    public static void register(JavalinConfig config, DataSource ds)
    {
        config.routes.get("/users", ctx ->
        {
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.getUsers(c));
            }
        });

        config.routes.put("/users/{id}", ctx ->
        {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            UpdateUserRequest body = ctx.bodyValidator(UpdateUserRequest.class)
                    .check(b -> b.currentPassword != null, "currentPassword is required")
                    .check(b -> b.password != null && !b.password.isEmpty(), "password is required")
                    .get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.updateUser(c, id, body.currentPassword, body.password));
            }
        });

        config.routes.delete("/users/{id}", ctx ->
        {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            try (Connection c = ds.getConnection())
            {
                Responses.ok(ctx, Postgres.deleteUser(c, id));
            }
        });
    }
}
