package chatapp.handlers;

import chatapp.Postgres;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            if (jsonResponse == null) {
                jsonResponse = new JSONObject().put("msg", "Something went wrong with the server.");
            }
            String response = jsonResponse.toString();

            // Only set a session cookie when authentication actually succeeded. A successful
            // login returns the user row (with user_id); failures return an {"msg": ...} object.
            if (jsonResponse.has("user_id")) {
                String username = URLEncoder.encode((String) jsonBody.get("username"), StandardCharsets.UTF_8.name());
                String sessionCookie = "session_id=" + username + "; Path=/; Max-Age=3600; HttpOnly; SameSite=Lax";
                httpExchange.getResponseHeaders().add("Set-Cookie", sessionCookie);
            }
            sendResponseToClient(httpExchange, response, 200);
        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
