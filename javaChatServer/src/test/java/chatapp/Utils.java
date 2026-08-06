package chatapp;

import chatapp.handlers.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class Utils
{
    private static final int PORT = 9230;
    private static Javalin server;
    private static HttpClient client = HttpClient.newHttpClient();

    public static void setClient(HttpClient client)
    {
        Utils.client = client;
    }

    public static void startServer()
    {
        String dbname = System.getenv().get("DB_TEST_NAME");
        String user = System.getenv().get("DB_TEST_USER");
        String password = System.getenv().get("DB_TEST_PASSWORD");
        String host = System.getenv().get("DB_HOST");
        String port = System.getenv().get("DB_PORT");

        if (dbname == null)
        { // Jenkins relies on environment variables, locally it relies on .env
            Dotenv dotenv = Dotenv.load();
            dbname = dotenv.get("DB_TEST_NAME");
            user = dotenv.get("DB_TEST_USER");
            password = dotenv.get("DB_TEST_PASSWORD");
        }
        Connection connection = Postgres.connectToDatabase(dbname, user, password, host != null ? host : "localhost",
                port != null ? port : "5432");
        Postgres.cleanDatabase(connection);
        /* Start chat server */
        server = Javalin.create(config ->
        {

            config.routes.exception(NumberFormatException.class, (e, ctx) ->
            {
                String message = "Invalid parameter format: " + e.getMessage();
                ctx.status(400)
                        .json(Map.of("error", message));
            });

            /* Handle wrong body format */
            config.routes.exception(ValidationException.class, (e, ctx) ->
            {
                String message = "Invalid body: " + e.getErrors().values().stream().flatMap(Collection::stream).map(
                        ValidationError::getMessage).findFirst().orElse("");
                ctx.status(400)
                        .json(Map.of("error", message));
            });

            try
            {
                ChatHandler.register(config, connection);
                LoginHandler.register(config, connection);
                LogoutHandler.register(config, connection);
                MessageHandler.register(config, connection);
                ProfilePictureHandler.register(config, connection);
                UserHandler.register(config, connection);
            } catch (IOException e)
            {
                System.out.println("Error registering handlers" + e.getMessage());
            }
        });
        System.out.println("Server started on port " + PORT);
        server.start(PORT);

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        setClient(HttpClient.newBuilder().cookieHandler(cookieManager).build());
    }

    public static void stopServer()
    {
        server.stop();
    }

    public static void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                                  int pExpectedStatusCode, String pExpectedResult)
    {
        sendRequestAndAssertResult(pDescription, pMethod, pEndpoint, pExpectedStatusCode, pExpectedResult, null);
    }

    public static void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                                  int pExpectedStatusCode, String pExpectedResult, String pBody)
    {
        System.out.println(pDescription);
        HttpRequest request = null;
        String DOMAIN = "http://localhost:";
        switch (pMethod)
        {
            case POST:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint))
                        .POST(HttpRequest.BodyPublishers.ofString(pBody != null ? pBody : "{}"))
                        .header("Content-Type", "application/json")
                        .build();
                break;
            case GET:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).GET()
                        .header("Content-Type", "application/json").build();
                break;
            case DELETE:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).DELETE()
                        .header("Content-Type", "application/json").build();
                break;
            case PUT:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint))
                        .PUT(HttpRequest.BodyPublishers.ofString(pBody)).header("Content-Type", "application/json")
                        .build();
                break;
            default:
        }
        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(pExpectedStatusCode, response.statusCode());
            assertEquals(pExpectedResult, response.body());
        } catch (IOException | InterruptedException e)
        {
            fail("Error sending request: " + e.getMessage());
        }
    }
}
