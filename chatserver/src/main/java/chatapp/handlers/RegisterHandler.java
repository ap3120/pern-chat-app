package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;

public class RegisterHandler implements HttpHandler {

    private Connection connection;
    public RegisterHandler(Connection connection) {
        this.connection = connection;
    }
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStreamReader requestBodyReader = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(requestBodyReader);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                requestBody.append(line);
            }
            bufferedReader.close();
            requestBodyReader.close();
            
            String response = requestBody.toString();
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStreamResponse = httpExchange.getResponseBody();
            outputStreamResponse.write(response.getBytes());
            outputStreamResponse.close();
        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
