package chatapp.handlers;

import chatapp.Postgres;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.sql.Connection;

public class LoginHandler extends AbstractHandler implements HttpHandler {
    public LoginHandler(Connection connection) {super(connection);}
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            OptionRequestHandler.handle(httpExchange);
        } else if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
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
            JSONObject jsonResponse = Postgres.login(connection, (String) jsonBody.get("username"), (String) jsonBody.get("password"));
            String response = jsonResponse.toString();

            // setting cookies
            HttpCookie sessionCookie = new HttpCookie("session_id", (String) jsonBody.get("username"));
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(3600);
            httpExchange.getResponseHeaders().add("Set-Cookie", sessionCookie.toString());
            sendResponseToClient(httpExchange, response, 200);
        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
