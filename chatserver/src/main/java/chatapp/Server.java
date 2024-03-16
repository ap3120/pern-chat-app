package chatapp;

import chatapp.handlers.RegisterHandler;
import chatapp.handlers.TestHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class Server {
    private static int port = 9000;
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        Connection connection = Postgres.connectToDatabase(dbname, user, password);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/test", new TestHandler());
            server.createContext("/register", new RegisterHandler(connection));
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
