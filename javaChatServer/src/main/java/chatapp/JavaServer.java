package chatapp;

import chatapp.handlers.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

public class JavaServer
{
    private final static int port = 9000;
    private final static int socketPort = 9200;

    public static void main(String[] args) throws IOException
    {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        String corsOrigin = dotenv.get("CORS_ORIGIN");
        Connection connection = Postgres.connectToDatabase(dbname, user, password, "localhost", "5432");

        JvmMetrics.builder()
                .register();

        Counter requestCounter = Counter.builder()
                .name("http_requests_total")
                .help("Total number of HTTP requests")
                .labelNames("method", "status")
                .register();

        /* Start metric endpoint */
        HTTPServer.builder()
                .port(9091)
                .buildAndStart();

        /* Start chat server */
        var server = Javalin.create(config ->
        {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule ->
            {
                rule.allowHost(corsOrigin);
                rule.allowCredentials = true;
            }));

            /* Handle wrong parameter format */
            config.routes.exception(NumberFormatException.class, (e, ctx) ->
            {
                String message = "Invalid parameter format: " + e.getMessage();
                ctx.status(400)
                        .json(Map.of("error", message));
                System.out.println(message);
            });

            /* Handle wrong body format */
            config.routes.exception(ValidationException.class, (e, ctx) ->
            {
                String message = "Invalid body: " + e.getErrors().values().stream().flatMap(Collection::stream).map(
                        ValidationError::getMessage).findFirst().orElse("");
                ctx.status(400)
                        .json(Map.of("error", message));
                System.out.println(message);
            });

            try
            {
                ChatHandler.register(config, connection);
                LoginHandler.register(config, connection);
                LogoutHandler.register(config, connection);
                MessageHandler.register(config, connection);
                ProfilePictureHandler.register(config, connection);
                UserHandler.register(config, connection);
            } catch (IOException e)
            {
                System.out.println("Error registering handlers" + e.getMessage());
            }

            if (requestCounter != null)
            {
                config.routes.after(ctx ->
                        requestCounter.labelValues(ctx.method()
                                        .name(), String.valueOf(ctx.statusCode()))
                                .inc());
            }
        });
        System.out.println("Server started on port " + port);
        server.start(port);

        /* Start websocket */
        Javalin socketServer = Javalin.create(config ->
        {
            config.routes.ws("/ws", wsConfig ->
            {
                wsConfig.onConnect(WebSocketServer::onOpen);
                wsConfig.onMessage(WebSocketServer::onMessage);
                wsConfig.onClose(WebSocketServer::onClose);
            });
        });
        socketServer.start("localhost", socketPort);
        System.out.println("WebSocket server started on ws://localhost:" + socketPort + "/ws");
    }
}
