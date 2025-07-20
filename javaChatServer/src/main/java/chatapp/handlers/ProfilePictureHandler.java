package chatapp.handlers;

import chatapp.Backend;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;

public class ProfilePictureHandler implements HttpHandler
{
    @Override
    public void handle(HttpExchange httpExchange)
    {
        String response = null;
        try
        {
            if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
            {
                OptionRequestHandler.handle(httpExchange);
            } else if (httpExchange.getRequestMethod().equalsIgnoreCase("POST"))
            {
                InputStreamReader requestBodyReader = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
                BufferedReader requestBody = new BufferedReader(requestBodyReader);
                String line;
                StringBuilder body = new StringBuilder();
                while ((line = requestBody.readLine()) != null)
                {
                    body.append(line);
                }
                requestBody.close();
                JSONObject jsonBody = new JSONObject(body.toString());
                String data = jsonBody.get("data").toString();
                BufferedWriter writer = new BufferedWriter(new FileWriter("/home/dev/Documents/kilo.png"));
                writer.write(data);
                writer.close();
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("msg", "Successfully write file");
                response = jsonResponse.toString();
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }

        Backend.sendResponse(httpExchange, response);
    }
}
