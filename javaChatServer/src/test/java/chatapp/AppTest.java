package chatapp;

import chatapp.handlers.*;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.validation.ValidationException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.sql.Connection;
import java.util.Map;

import static chatapp.Utils.sendRequestAndAssertResult;
import static chatapp.Utils.setClient;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest
{
    private static Javalin server;
    private static final int PORT = 9230;

    @BeforeAll
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

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        setClient(HttpClient.newBuilder().cookieHandler(cookieManager).build());
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
        sendRequestAndAssertResult("Add user with malformed body", RequestMethodEnum.POST, "/users", 400,
                "{\"error\":\"Invalid body.\"}", "{\"password\": \"bob_pwd\"}");
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
        sendRequestAndAssertResult("Test update user", RequestMethodEnum.PUT, "/users", 200,
                "{\"msg\":\"Password successfully updated\"}",
                "{\"currentPassword\":\"alice_pwd\",\"password\": \"new_alice_pwd\"}");
    }

    @Test
    @Order(5)
    public void testRemoveUser()
    {
        sendRequestAndAssertResult("Test remove user", RequestMethodEnum.DELETE, "/users", 200,
                "{\"msg\":\"User successfully deleted.\"}");
    }

    @Test
    @Order(6)
    public void testLogout()
    {
        sendRequestAndAssertResult("Test login with correct password", RequestMethodEnum.POST, "/login", 200,
                "{\"user_id\":2,\"username\":\"Bob\"}", "{\"username\": \"Bob\",\"password\": \"bob_pwd\"}");
        sendRequestAndAssertResult("Test logout user", RequestMethodEnum.POST, "/logout", 200,
                "{\"msg\":\"User logged out.\"}");
    }

    @Test
    @Order(7)
    public void testUpdateUserWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test update user when logged out", RequestMethodEnum.PUT, "/users", 401,
                "{\"error\":\"User not logged in\"}",
                "{\"currentPassword\":\"alice_pwd\",\"password\": \"new_alice_pwd\"}");
    }

    @Test
    @Order(8)
    public void testRemoveUserWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test remove user when logged out", RequestMethodEnum.DELETE, "/users", 401,
                "{\"error\":\"User not logged in\"}");
    }
}
