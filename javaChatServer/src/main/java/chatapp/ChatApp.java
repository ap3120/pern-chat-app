package chatapp;

import chatapp.controllers.AuthController;
import chatapp.controllers.ChatController;
import chatapp.controllers.MessageController;
import chatapp.controllers.Responses;
import chatapp.controllers.UserController;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.validation.ValidationException;
import io.prometheus.metrics.core.metrics.Counter;
import org.json.JSONObject;

import javax.sql.DataSource;

/**
 * Builds the Javalin REST app and the WebSocket app from a single place so that
 * {@link JavaServer} and the tests wire up identical routing. All routes are registered
 * inside Javalin.create(...) because Javalin 7 forbids adding routes after start().
 */
public final class ChatApp
{
    private ChatApp() {}

    public static HikariDataSource buildDataSource(String host, String port, String dbname,
                                                   String user, String password)
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbname);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setPoolName("chatapp-pool");
        return new HikariDataSource(config);
    }

    /**
     * @param requestCounter Prometheus counter to increment per request, or null (tests) to skip metrics.
     */
    public static Javalin buildRestApp(DataSource ds, String corsOrigin, Counter requestCounter)
    {
        return Javalin.create(config ->
        {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule ->
            {
                rule.allowHost(corsOrigin);
                rule.allowCredentials = true;
            }));

            AuthController.register(config, ds);
            UserController.register(config, ds);
            ChatController.register(config, ds);
            MessageController.register(config, ds);

            // Bad input (path param / body validation) -> 400, before the generic 500 mapper.
            config.routes.exception(ValidationException.class, (e, ctx) ->
                    ctx.status(400).contentType(Responses.JSON)
                            .result(new JSONObject().put("msg", "Invalid request.").toString()));
            config.routes.exception(Exception.class, (e, ctx) ->
            {
                e.printStackTrace();
                Responses.serverError(ctx);
            });

            // Record the real status per request (the old MetricsHandler hardcoded 200).
            if (requestCounter != null)
            {
                config.routes.after(ctx ->
                        requestCounter.labelValues(ctx.method().name(), String.valueOf(ctx.statusCode())).inc());
            }
        });
    }

    public static Javalin buildWsApp()
    {
        return Javalin.create(config ->
                config.routes.ws("/ws", ws ->
                {
                    ws.onConnect(WsHub::onConnect);
                    ws.onMessage(WsHub::onMessage);
                    ws.onClose(WsHub::onClose);
                }));
    }
}
