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

public class ChatHandler implements HttpHandler {
    private Connection connection;
    public ChatHandler(Connection connection) {this.connection = connection;}
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
                JSONObject jsonResponse = Postgres.addToChats(connection, (int) jsonBody.get("created_by"), (LocalDateTime) jsonBody.get("created_at"));
                response = jsonResponse.toString();
            }
        } else if ((arrayPath.length == 3) && arrayPath[2].equals("users_chats")) {
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
                JSONObject jsonResponse = Postgres.addToUsersChats(connection, (int) jsonBody.get("user_id"), (int) jsonBody.get("chat_id"));
                response = jsonResponse.toString();
            }
        } else if (arrayPath.length == 2) {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                JSONArray jsonResponse = Postgres.readFromChats(connection, Integer.parseInt(arrayPath[1]));
                response = jsonResponse.toString();
            }
        } else if (arrayPath.length == 4) {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                JSONObject jsonResponse = Postgres.getUser(connection, Integer.parseInt(arrayPath[2]), Integer.parseInt(arrayPath[3]));
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
