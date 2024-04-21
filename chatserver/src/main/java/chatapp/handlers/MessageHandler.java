package chatapp.handlers;

import chatapp.Postgres;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;

public class MessageHandler implements HttpHandler {
    private Connection connection;
    public MessageHandler(Connection connection) {this.connection = connection;}
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        System.out.println(path);
        String[] arrayPath = path.split("/");
        System.out.println(Arrays.toString(arrayPath));
        String response = null;
        if (arrayPath.length == 1) {
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
                String body = requestBody.toString();
                JSONObject jsonBody = new JSONObject(body);
                JSONObject jsonResponse = Postgres.addMessage(connection,
                        (String) jsonBody.get("content"),
                        (int) jsonBody.get("sender_id"),
                        (String) jsonBody.get("sender_username"),
                        (int) jsonBody.get("receiver_id"),
                        (String) jsonBody.get("receiver_username"),
                        (int) jsonBody.get("chat_id"),
                        (LocalDateTime) jsonBody.get("created_at"));
                response = jsonResponse.toString();
            }
        } else if (arrayPath.length == 2) {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                JSONArray jsonResponse = Postgres.getMessagesFromChat(connection, Integer.parseInt(arrayPath[1]));
                response = jsonResponse.toString();
            }
        }
        if (response != null) {
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
