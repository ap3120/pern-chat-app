package chatapp;

import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

public class JavaServer
{
    public static void main(String[] args) throws IOException
    {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        String dbHost = orDefault(dotenv.get("DB_HOST"), "localhost");
        String dbPort = orDefault(dotenv.get("DB_PORT"), "5432");

        int restPort = Integer.parseInt(orDefault(dotenv.get("SERVER_PORT"), "9000"));
        int wsPort = Integer.parseInt(orDefault(dotenv.get("WEBSOCKET_PORT"), "9200"));
        int metricsPort = Integer.parseInt(orDefault(dotenv.get("METRICS_PORT"), "9091"));
        String corsOrigin = orDefault(dotenv.get("CORS_ORIGIN"), "http://localhost:3000");

        HikariDataSource ds = ChatApp.buildDataSource(dbHost, dbPort, dbname, user, password);

        JvmMetrics.builder().register();
        Counter requestCounter = Counter.builder()
                .name("http_requests_total")
                .help("Total number of HTTP requests")
                .labelNames("method", "status")
                .register();

        // Prometheus metrics endpoint (unchanged port so prometheus.yml keeps scraping :9091).
        HTTPServer.builder()
                .port(metricsPort)
                .buildAndStart();

        Javalin rest = ChatApp.buildRestApp(ds, corsOrigin, requestCounter);
        rest.start(restPort);
        System.out.println("REST server started on port " + restPort);

        // WebSocket runs as its own Javalin instance on the dedicated port the frontend expects,
        // bound to localhost to match the previous Tyrus binding.
        Javalin ws = ChatApp.buildWsApp();
        ws.start("localhost", wsPort);
        System.out.println("WebSocket server started on ws://localhost:" + wsPort + "/ws");
    }

    private static String orDefault(String value, String fallback)
    {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
