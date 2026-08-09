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
                "{\"error\":\"Invalid body: created_by must be an integer\"}", "{\"created_by\":\"strange body\"}");
    }

    @Test
    @Order(4)
    public void testCreateChat()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test create chat", RequestMethodEnum.POST, "/chat", 200,
                "{\"chat_id\":1,\"created_by\":1,\"created_at\":\"" + now + "\"}", "{\"created_by\":1}");
    }

    @Test
    @Order(5)
    public void testAddParticipantToChatWithMalformedBody()
    {
        sendRequestAndAssertResult("Test add participant to chat with invalid user_id", RequestMethodEnum.POST,
                "/chat/users_chats", 400, "{\"error\":\"Invalid body: user_id must be an integer\"}",
                "{\"user_id\": \"Not an integer\", \"chat_id\": 1}");
        sendRequestAndAssertResult("Test add participant to chat with missing user_id field", RequestMethodEnum.POST,
                "/chat/users_chats", 400, "{\"error\":\"Invalid body: user_id must be an integer\"}",
                "{\"chat_id\": 1}");
        sendRequestAndAssertResult("Test add participant to chat with invalid chat_id", RequestMethodEnum.POST,
                "/chat/users_chats", 400, "{\"error\":\"Invalid body: chat_id must be an integer\"}",
                "{\"user_id\": \"1\", \"chat_id\": \"Not an integer\"}");
        sendRequestAndAssertResult("Test add participant to chat with missing chat_id field", RequestMethodEnum.POST,
                "/chat/users_chats", 400, "{\"error\":\"Invalid body: chat_id must be an integer\"}",
                "{\"user_id\": 1}");
    }

    @Test
    @Order(6)
    public void testAddParticipantToChat()
    {
        sendRequestAndAssertResult("Test add Alice user to chat", RequestMethodEnum.POST,
                "/chat/users_chats", 200, "{\"user_id\":1,\"chat_id\":1}",
                "{\"user_id\": 1, \"chat_id\": 1}");
        sendRequestAndAssertResult("Test add Bob user to chat", RequestMethodEnum.POST,
                "/chat/users_chats", 200, "{\"user_id\":2,\"chat_id\":1}",
                "{\"user_id\": 2, \"chat_id\": 1}");
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
                "{\"error\":\"User not logged in\"}", "{\"created_by\":1}");
    }

    @Test
    @Order(10)
    public void testAddParticipantToChatWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test add Alice user to chat when logged out", RequestMethodEnum.POST,
                "/chat/users_chats", 401, "{\"error\":\"User not logged in\"}",
                "{\"user_id\": 1, \"chat_id\": 1}");
    }

    @Test
    @Order(11)
    public void testGetChatsWhenLoggedOut()
    {
        sendRequestAndAssertResult("Get Alice chats when logged out", RequestMethodEnum.GET, "/chat", 401,
                "{\"error\":\"User not logged in\"}");
    }
}
