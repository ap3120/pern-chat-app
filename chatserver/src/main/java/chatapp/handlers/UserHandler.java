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

public class UserHandler implements HttpHandler {

    private Connection connection;

    public UserHandler(Connection connection) {
        this.connection = connection;
    }
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String[] pathArray = path.split("/");
        String response = null;

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            OptionRequestHandler.handle(httpExchange);
        } else if (httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
            JSONArray jsonResponse = Postgres.getUsers(connection);
            response = jsonResponse.toString();
        } else if (httpExchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            InputStreamReader requestBodyReader = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(requestBodyReader);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                requestBody.append(line);
            }
            bufferedReader.close();
            requestBodyReader.close();
            String body = requestBody.toString();
            JSONObject jsonBody = new JSONObject(body);
            JSONObject jsonResponse = Postgres.updateUser(connection, Integer.parseInt(pathArray[2]), (String) jsonBody.get("currentPassword"), (String) jsonBody.get("password"));
            response = jsonResponse.toString();
        } else if (httpExchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            JSONObject jsonResponse = Postgres.deleteUser(connection, Integer.parseInt(pathArray[2]));
            response = jsonResponse.toString();
        }

        if (response != null) {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
