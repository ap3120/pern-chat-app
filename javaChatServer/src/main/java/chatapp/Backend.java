package chatapp;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class Backend
{
    public static void sendResponse(HttpExchange httpExchange, String response) {

        try {
            if (response != null) {
                httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream outputStream = httpExchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
            } else {
                httpExchange.sendResponseHeaders(405, -1);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
