package chatapp;

import chatapp.handlers.*;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class JavaServer {
    private static int port = 9000;
    private static int socketPort = 8080;

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        Connection connection = Postgres.connectToDatabase(dbname, user, password);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server started on port " + port);
            server.createContext("/register", new RegisterHandler(connection));
            server.createContext("/login", new LoginHandler(connection));
            server.createContext("/logout", new LogoutHandler(connection));
            server.createContext("/chat", new ChatHandler(connection));
            server.createContext("/message", new MessageHandler(connection));
            server.createContext("/users", new UserHandler(connection));
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Server server = new Server("localhost", socketPort, "/", null, WebSocketServer.class);
        try {
            server.start();
            System.out.println("WebSocket server started on ws://localhost:8080/ws");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
