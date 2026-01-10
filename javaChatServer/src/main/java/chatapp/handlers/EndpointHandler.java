package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

public class EndpointHandler
{
    protected final Connection connection;

    public EndpointHandler(Connection pConnection)
    {
        connection = pConnection;
    }

    public void sendResponseToClient(HttpExchange pHttpExchange, String pResponse, int pStatusCode)
    {
        try
        {
            pHttpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            pHttpExchange.sendResponseHeaders(pStatusCode, pResponse.length());
            OutputStream outputStream = pHttpExchange.getResponseBody();
            outputStream.write(pResponse.getBytes());
            outputStream.close();
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
