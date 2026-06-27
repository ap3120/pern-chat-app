package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class AbstractHandler
{
    protected final Connection connection;

    public AbstractHandler(Connection pConnection)
    {
        connection = pConnection;
    }

    public void sendResponseToClient(HttpExchange pHttpExchange, String pResponse, int pStatusCode)
    {
        try
        {
            byte[] bytes = pResponse.getBytes(StandardCharsets.UTF_8);
            pHttpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            pHttpExchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            // Use the byte length, not the char count, so multi-byte UTF-8 bodies are not truncated.
            pHttpExchange.sendResponseHeaders(pStatusCode, bytes.length);
            try (OutputStream outputStream = pHttpExchange.getResponseBody())
            {
                outputStream.write(bytes);
            }
        } catch (IOException e)
        {
            System.out.println("Error sending response: " + e.getMessage());
        }
    }
}
