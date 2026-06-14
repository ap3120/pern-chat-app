package chatapp;

import chatapp.handlers.*;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class JavaServer
{
    private final static int port = 9000;
    private final static int socketPort = 9200;

    public static void main(String[] args)
    {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        Connection connection = Postgres.connectToDatabase(dbname, user, password, "localhost", "5432");

        JvmMetrics.builder().register();

        Counter requestCounter = Counter.builder()
                .name("http_requests_total")
                .help("Total number of HTTP requests")
                .labelNames("method", "status")
                .register();

        try
        {
            /* Start metric endpoint */
            HTTPServer.builder()
                    .port(9091)
                    .buildAndStart();

            /* Start chat server */
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server started on port " + port);
            server.createContext(
                    "/register",
                    new MetricsHandler(new UserHandler(connection), requestCounter)
            );
            server.createContext(
                    "/login",
                    new MetricsHandler(new LoginHandler(connection), requestCounter)
            );
            server.createContext(
                    "/logout",
                    new MetricsHandler(new LogoutHandler(connection), requestCounter)
            );
            server.createContext(
                    "/chat",
                    new MetricsHandler(new ChatHandler(connection), requestCounter)
            );
            server.createContext(
                    "/message",
                    new MetricsHandler(new MessageHandler(connection), requestCounter)
            );
            server.createContext(
                    "/users",
                    new MetricsHandler(new UserHandler(connection), requestCounter)
            );
            server.setExecutor(null);
            server.start();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        /* Start websocket */
        Server server = new Server("localhost", socketPort, "/", null, WebSocketServer.class);
        try
        {
            server.start();
            System.out.println("WebSocket server started on ws://localhost:" + socketPort + "/ws");
            Thread.currentThread().join();
        } catch (Exception e)
        {
            System.out.println("Error starting websocket: " + e.getMessage());
        }
    }
}
