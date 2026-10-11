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

    public static void main(String[] args) throws IOException
    {
        boolean testMode = args.length == 1 && args[0].equals("test");
        Dotenv dotenv = Dotenv.load();
        String dbname = testMode ? dotenv.get("DB_TEST_NAME") : dotenv.get("DB_NAME");
        String user = testMode ? dotenv.get("DB_TEST_USER") : dotenv.get("DB_USER");
        String password = testMode ? dotenv.get("DB_TEST_PASSWORD") : dotenv.get("DB_PASSWORD");
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
                if (testMode)
                {
                    TestHandler.register(config, connection);
                }
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

            config.routes.ws("/ws", wsConfig ->
            {
                wsConfig.onConnect(WebSocketServer::onOpen);
                wsConfig.onMessage(WebSocketServer::onMessage);
                wsConfig.onClose(WebSocketServer::onClose);
            });
        });
        System.out.println((testMode ? "Test server " : "Server ") + "started on port " + port);
        server.start(port);
    }
}
