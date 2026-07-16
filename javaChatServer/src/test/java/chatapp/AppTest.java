package chatapp;

import chatapp.handlers.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.validation.ValidationException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest
{
    private static Javalin server;
    private static final int PORT = 9230;
    private final String DOMAIN = "http://localhost:";

    @BeforeAll
    public static void startServer()
    {
        String dbname = System.getenv().get("DB_TEST_NAME");
        String user = System.getenv().get("DB_TEST_USER");
        String password = System.getenv().get("DB_TEST_PASSWORD");
        String host = System.getenv().get("DB_HOST");
        String port = System.getenv().get("DB_PORT");

        if (dbname == null)
        { // Jenkins relies on environment variables and locally it relies on .env
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

            /* Handle wrong parameter format */
            config.routes.exception(NumberFormatException.class, (e, ctx) ->
            {
                ctx.status(400)
                        .json(Map.of("error", "Invalid parameter format"));
            });

            /* Handle wrong body format */
            config.routes.exception(ValidationException.class, (e, ctx) ->
            {
                ctx.status(400)
                        .json(Map.of("error", "Invalid body."));
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
    }

    @AfterAll
    public static void clean()
    {
        server.stop();
    }

    @Test
    @Order(1)
    public void testRegistration()
    {
        sendRequestAndAssertResult("Add new Alice user", RequestMethodEnum.POST, "/users", 200,
                "{\"user_id\":1,\"username\":\"Alice\"}", "{\"username\": \"Alice\", \"password\": \"alice_pwd\"}");
        sendRequestAndAssertResult("Add new Bob user", RequestMethodEnum.POST, "/users", 200,
                "{\"user_id\":2,\"username\":\"Bob\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
        sendRequestAndAssertResult("Add new Charlie user", RequestMethodEnum.POST, "/users", 200,
                "{\"user_id\":3,\"username\":\"Charlie\"}",
                "{\"username\": \"Charlie\", \"password\": \"charlie_pwd\"}");
        sendRequestAndAssertResult("Add existing Bob user", RequestMethodEnum.POST, "/users", 200,
                "{\"error\":\"User Bob already exists.\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
    }

    @Test
    @Order(2)
    public void testListUsers()
    {
        sendRequestAndAssertResult("List users", RequestMethodEnum.GET, "/users", 200,
                "[{\"user_id\":1,\"username\":\"Alice\"},{\"user_id\":2,\"username\":\"Bob\"},{\"user_id\":3,\"username\":\"Charlie\"}]");
    }

    @Test
    @Order(3)
    public void testLogin()
    {
        sendRequestAndAssertResult("Test login with non existing user", RequestMethodEnum.POST, "/login", 200,
                "{\"error\":\"User Louis doesn't exist.\"}", "{\"username\": \"Louis\",\"password\": \"louis_pwd\"}");
        sendRequestAndAssertResult("Test login with wrong password", RequestMethodEnum.POST, "/login", 200,
                "{\"error\":\"Wrong password for Alice\"}", "{\"username\": \"Alice\",\"password\": \"wrong_pwd\"}");
        sendRequestAndAssertResult("Test login with correct password", RequestMethodEnum.POST, "/login", 200,
                "{\"user_id\":1,\"username\":\"Alice\"}", "{\"username\": \"Alice\",\"password\": \"alice_pwd\"}");
    }

    @Test
    @Order(4)
    public void testUpdateUser()
    {
        sendRequestAndAssertResult("Test update user", RequestMethodEnum.PUT, "/users/1", 200,
                "{\"msg\":\"Password successfully updated\"}",
                "{\"currentPassword\":\"alice_pwd\",\"password\": \"new_alice_pwd\"}");
    }

    @Test
    @Order(5)
    public void testRemoveUser()
    {
        sendRequestAndAssertResult("Test remove user", RequestMethodEnum.DELETE, "/users/1", 200,
                "{\"msg\":\"User successfully deleted.\"}");
    }

    private void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                            int pExpectedStatusCode, String pExpectedResult)
    {
        sendRequestAndAssertResult(pDescription, pMethod, pEndpoint, pExpectedStatusCode, pExpectedResult, null);
    }

    private void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                            int pExpectedStatusCode, String pExpectedResult, String pBody)
    {
        System.out.println(pDescription);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = null;
        switch (pMethod)
        {
            case POST:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint))
                        .POST(HttpRequest.BodyPublishers.ofString(pBody)).header("Content-Type", "application/json")
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
