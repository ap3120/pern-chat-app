package chatapp;

import chatapp.handlers.RegisterHandler;
import chatapp.handlers.UserHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest
{
    private static HttpServer server;
    private static final int PORT = 9000;
    private final String DOMAIN = "http://localhost:";

    @BeforeAll
    public static void startServer()
    {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_TEST_NAME");
        String user = dotenv.get("DB_TEST_USER");
        String password = dotenv.get("DB_TEST_PASSWORD");
        Connection connection = Postgres.connectToDatabase(dbname, user, password);
        System.out.println("I run");
        Postgres.cleanDatabase(connection);
        try
        {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/register", new RegisterHandler(connection));
            server.createContext("/users", new UserHandler(connection));
            server.start();
        } catch (IOException pException)
        {
            System.out.println("Error starting server: " + pException.getMessage());
        }
    }

    @AfterAll
    public static void clean()
    {
        server.stop(0); // delay for stopping is zero
    }

    @Test
    @Order(1)
    public void testRegistration()
    {
        sendRequestAndAssertResult("Add new Alice user", RequestMethodEnum.POST, "/register", 200,
                "{\"user_id\":1,\"username\":\"Alice\"}", "{\"username\": \"Alice\", \"password\": \"alice_pwd\"}");
        sendRequestAndAssertResult("Add new Bob user", RequestMethodEnum.POST, "/register", 200,
                "{\"user_id\":2,\"username\":\"Bob\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
        sendRequestAndAssertResult("Add existing Bob user", RequestMethodEnum.POST, "/register", 200,
                "{\"msg\":\"User Bob already exists.\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
    }

    @Test
    @Order(2)
    public void testListUsers()
    {
        sendRequestAndAssertResult("List users", RequestMethodEnum.GET, "/users", 200,
                "[{\"user_id\":1,\"username\":\"Alice\"},{\"user_id\":2,\"username\":\"Bob\"}]");
    }

    private void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                            int pExpectedStatusCode, String pExpectedResult)
    {
        sendRequestAndAssertResult(pDescription, pMethod, pEndpoint, pExpectedStatusCode, pExpectedResult, null);
    }

    private void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                            int pExpectedStatusCode, String pExpectedResult, String pBody)
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = null;
        switch (pMethod)
        {
            case POST:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).POST(
                        HttpRequest.BodyPublishers.ofString(pBody)).header("Content-Type", "application/json").build();
                break;
            case GET:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).GET().header(
                        "Content-Type", "application/json").build();
                break;
            case DELETE:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).DELETE().header(
                        "Content-Type", "application/json").build();
                break;
            case PUT:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).PUT(
                        HttpRequest.BodyPublishers.ofString(pBody)).header("Content-Type", "application/json").build();
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
