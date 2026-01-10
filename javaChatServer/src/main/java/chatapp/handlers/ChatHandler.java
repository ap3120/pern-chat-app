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

import static java.lang.Integer.parseInt;

public class ChatHandler extends EndpointHandler implements HttpHandler
{
    public ChatHandler(Connection connection)
    {
        super(connection);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String path = httpExchange.getRequestURI().getPath();
        String[] arrayPath = path.split("/");
        String response = null;
        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
        {
            OptionRequestHandler.handle(httpExchange);
        } else if (arrayPath.length == 2 && httpExchange.getRequestMethod().equalsIgnoreCase("POST"))
        {
            InputStreamReader requestBodyReader = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader bufferedReader = new BufferedReader(requestBodyReader);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                requestBody.append(line);
            }
            bufferedReader.close();
            requestBodyReader.close();
            String body = requestBody.toString();
            JSONObject jsonBody = new JSONObject(body);
            //System.out.println(jsonBody.get("created_by"));
            JSONObject jsonResponse = Postgres.addToChat(connection,
                    Integer.parseInt((String) jsonBody.get("created_by")));
            //System.out.println(jsonResponse.toString());
            response = jsonResponse != null ? jsonResponse.toString() : null;
            //System.out.println(response);
        } else if ((arrayPath.length == 3))
        {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST"))
            {
                InputStreamReader requestBodyReader = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                BufferedReader bufferedReader = new BufferedReader(requestBodyReader);
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    requestBody.append(line);
                }
                bufferedReader.close();
                requestBodyReader.close();
                String body = requestBody.toString();
                JSONObject jsonBody = new JSONObject(body);
                JSONObject jsonResponse = Postgres.addToUsersChats(connection, (int) jsonBody.get("user_id"),
                        (int) jsonBody.get("chat_id"));
                response = jsonResponse != null ? jsonResponse.toString() : null;
            } else if (httpExchange.getRequestMethod().equalsIgnoreCase("GET"))
            {
                JSONArray jsonResponse = Postgres.readFromChats(connection, parseInt(arrayPath[2]));
                response = jsonResponse != null ? jsonResponse.toString() : null;
            }
        } else if (arrayPath.length == 5 && httpExchange.getRequestMethod().equalsIgnoreCase("GET"))
        {
            JSONObject jsonResponse = Postgres.getUser(connection, parseInt(arrayPath[3]), parseInt(arrayPath[4]));
            response = jsonResponse != null ? jsonResponse.toString() : null;
        }
        if (response != null)
        {
            httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        } else
        {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}
