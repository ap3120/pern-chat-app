package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;

public class LogoutHandler implements HttpHandler {
    private Connection connection;
    public LogoutHandler(Connection connection) {this.connection = connection;}
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
