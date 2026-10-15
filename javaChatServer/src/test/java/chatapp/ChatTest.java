package chatapp;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static chatapp.Utils.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatTest
{
    @BeforeAll
    public static void beforeAll()
    {
        startServer();
    }

    @AfterAll
    public static void afterAll()
    {
        stopServer();
    }

    @Test
    @Order(1)
    public void addUsers()
    {
        sendRequestAndAssertResult("Add new Alice user", RequestMethodEnum.POST, "/users", 200,
                "{\"user_id\":1,\"username\":\"Alice\"}", "{\"username\": \"Alice\", \"password\": \"alice_pwd\"}");
        sendRequestAndAssertResult("Add new Bob user", RequestMethodEnum.POST, "/users", 200,
                "{\"user_id\":2,\"username\":\"Bob\"}", "{\"username\": \"Bob\", \"password\": \"bob_pwd\"}");
    }

    @Test
    @Order(2)
    public void testLogin()
    {
        sendRequestAndAssertResult("Test login with correct password", RequestMethodEnum.POST, "/login", 200,
                "{\"user_id\":1,\"username\":\"Alice\"}", "{\"username\": \"Alice\",\"password\": \"alice_pwd\"}");
    }

    @Test
    @Order(3)
    public void testCreateChatWithMalformedBody()
    {
        sendRequestAndAssertResult("Test create chat with malformed body", RequestMethodEnum.POST, "/chat", 400,
                "{\"error\":\"Invalid body: created_by must be an integer\"}",
                "{\"created_by\":\"strange body\",\"other_user_id\":1}");
    }

    @Test
    @Order(4)
    public void testCreateChatWithSameUser()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test create chat", RequestMethodEnum.POST, "/chat", 409,
                "{\"error\":\"A chat requires two different users\"}", "{\"created_by\":1,\"other_user_id\":1}");
    }

    @Test
    @Order(5)
    public void testCreateChat()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test create chat", RequestMethodEnum.POST, "/chat", 200,
                "{\"chat_id\":1,\"created_by\":1,\"created_at\":\"" + now + "\"}",
                "{\"created_by\":1,\"other_user_id\":2}");
    }

    @Test
    @Order(6)
    public void testCreateDuplicateChat()
    {
        sendRequestAndAssertResult("Test create chat", RequestMethodEnum.POST, "/chat", 409,
                "{\"error\":\"Chat already exists\"}", "{\"created_by\":2,\"other_user_id\":1}");
    }

    @Test
    @Order(7)
    public void testGetChats()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Get Alice chats", RequestMethodEnum.GET, "/chat", 200,
                "[{\"user_id\":2,\"username\":\"Bob\",\"created_at\":\"" + now + "\",\"created_by\":1,\"chat_id\":1}]");
    }

    @Test
    @Order(8)
    public void testLogout()
    {
        sendRequestAndAssertResult("Test logout", RequestMethodEnum.POST, "/logout", 200,
                "{\"msg\":\"User logged out.\"}");
    }

    @Test
    @Order(9)
    public void testCreateChatWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test create chat when logged out", RequestMethodEnum.POST, "/chat", 401,
                "{\"error\":\"User not logged in\"}", "{\"created_by\":1,\"other_user_id\":2}");
    }

    @Test
    @Order(10)
    public void testGetChatsWhenLoggedOut()
    {
        sendRequestAndAssertResult("Get Alice chats when logged out", RequestMethodEnum.GET, "/chat", 401,
                "{\"error\":\"User not logged in\"}");
    }
}
