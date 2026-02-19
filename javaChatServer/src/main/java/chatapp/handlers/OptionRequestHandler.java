package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class OptionRequestHandler
{
    public static void handle(HttpExchange httpExchange) throws IOException
    {
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpExchange.getResponseHeaders().add("Access-Control-Max-Age", "86400"); // 24 hours

        // Respond with OK status code
        httpExchange.sendResponseHeaders(200, -1);
        httpExchange.close();
    }
}
