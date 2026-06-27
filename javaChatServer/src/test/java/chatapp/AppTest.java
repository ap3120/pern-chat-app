package chatapp;

import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest
{
    private static Javalin server;
    private static HikariDataSource ds;
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
        ds = ChatApp.buildDataSource(host != null ? host : "localhost", port != null ? port : "5432",
                dbname, user, password);
        try (Connection connection = ds.getConnection())
        {
            Postgres.cleanDatabase(connection);
        } catch (SQLException pException)
        {
            System.out.println("Error cleaning database: " + pException.getMessage());
        }
        // Build from the same factory main() uses, so tests exercise the real routing
        // (no Prometheus counter in tests -> pass null).
        server = ChatApp.buildRestApp(ds, "http://localhost:3000", null);
        server.start(PORT);
    }

    @AfterAll
    public static void clean()
    {
        if (server != null) server.stop();
        if (ds != null) ds.close();
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

    /**
     * Regression test for the SQL injection that the string-concatenated queries allowed.
     * A username carrying a DROP TABLE payload must be stored as a literal value, and the
     * users table must still exist and be intact afterwards.
     */
    @Test
    @Order(6)
    public void testSqlInjectionIsNeutralized()
    {
        // Users 2 (Bob) and 3 (Charlie) remain after the previous tests; this becomes user 4.
        sendRequestAndAssertResult("Register a user with a SQL injection payload as username",
                RequestMethodEnum.POST, "/register", 200,
                "{\"user_id\":4,\"username\":\"Mallory'); DROP TABLE users; --\"}",
                "{\"username\": \"Mallory'); DROP TABLE users; --\", \"password\": \"mallory_pwd\"}");

        // If the payload had executed, this query would fail or return fewer rows.
        sendRequestAndAssertResult("Users table is intact after the injection attempt",
                RequestMethodEnum.GET, "/users", 200,
                "[{\"user_id\":2,\"username\":\"Bob\"},{\"user_id\":3,\"username\":\"Charlie\"}," +
                        "{\"user_id\":4,\"username\":\"Mallory'); DROP TABLE users; --\"}]");
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
