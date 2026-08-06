package chatapp;

import org.junit.jupiter.api.*;

import static chatapp.Utils.sendRequestAndAssertResult;

/**
 * Integration tests for /users route.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest
{
    @BeforeAll
    public static void startServer()
    {
        Utils.startServer();
    }

    @AfterAll
    public static void clean()
    {
        Utils.stopServer();
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
                "{\"error\":\"Invalid body: username is required\"}", "{\"password\": \"bob_pwd\"}");
    }
    
    @Test
    @Order(2)
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
    @Order(3)
    public void testListUsers()
    {
        sendRequestAndAssertResult("List users", RequestMethodEnum.GET, "/users", 200,
                "[{\"user_id\":1,\"username\":\"Alice\"},{\"user_id\":2,\"username\":\"Bob\"},{\"user_id\":3,\"username\":\"Charlie\"}]");
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
