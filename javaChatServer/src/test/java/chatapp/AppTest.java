package chatapp;

import chatapp.handlers.LoginHandler;
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
        try
        {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/register", new RegisterHandler(connection));
            server.createContext("/users", new UserHandler(connection));
            server.createContext("/login", new LoginHandler(connection));
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
        sendRequestAndAssertResult("Add new Charlie user", RequestMethodEnum.POST, "/register", 200,
                "{\"user_id\":3,\"username\":\"Charlie\"}",
                "{\"username\": \"Charlie\", \"password\": \"charlie_pwd\"}");
        sendRequestAndAssertResult("Add existing Bob user", RequestMethodEnum.POST, "/register", 200,
                "{\"msg\":\"User Bob already exists.\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
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
                "{\"msg\":\"User Louis doesn't exist.\"}", "{\"username\": \"Louis\",\"password\": \"louis_pwd\"}");
        sendRequestAndAssertResult("Test login with wrong password", RequestMethodEnum.POST, "/login", 200,
                "{\"msg\":\"Wrong password for Alice\"}", "{\"username\": \"Alice\",\"password\": \"wrong_pwd\"}");
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
