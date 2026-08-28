package chatapp;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static chatapp.Utils.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTest
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
    public void testAddUsers()
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
    public void testCreateChat()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test create chat", RequestMethodEnum.POST, "/chat", 200,
                "{\"chat_id\":1,\"created_by\":1,\"created_at\":\"" + now + "\"}", "{\"created_by\":1}");
    }

    @Test
    @Order(4)
    public void testSendMessageWithMalformedBody()
    {
        sendRequestAndAssertResult("Test send message with malformed body", RequestMethodEnum.POST, "/message", 400,
                "{\"error\":\"Invalid body: sender_username is required.\"}",
                "{\"content\":\"Hello Bob this is Alice.\",\"receiver_id\":2,\"receiver_username\":\"Bob\",\"chat_id\":1}");
    }

    @Test
    @Order(5)
    public void testSendMessage()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test send message", RequestMethodEnum.POST, "/message", 200,
                "{\"sender_username\":\"Alice\",\"receiver_id\":2,\"send_at\":\"" + now + "\",\"message_id\":1,\"content\":\"Hello Bob this is Alice.\",\"sender_id\":1,\"chat_id\":1,\"receiver_username\":\"Bob\"}",
                "{\"content\":\"Hello Bob this is Alice.\",\"sender_username\":\"Alice\",\"receiver_id\":2,\"receiver_username\":\"Bob\",\"chat_id\":1}");
    }

    @Test
    @Order(6)
    public void testGetMessagesFromChat()
    {
        LocalDateTime now = LocalDateTime.now();
        sendRequestAndAssertResult("Test get messages from chat", RequestMethodEnum.GET, "/message/1", 200,
                "[{\"sender_username\":\"Alice\",\"receiver_id\":2,\"send_at\":\"" + now + "\",\"message_id\":1,\"content\":\"Hello Bob this is Alice.\",\"sender_id\":1,\"chat_id\":1,\"receiver_username\":\"Bob\"}]");
    }

    @Test
    @Order(7)
    public void testLogout()
    {
        sendRequestAndAssertResult("Test logout", RequestMethodEnum.POST, "/logout", 200,
                "{\"msg\":\"User logged out.\"}");
    }

    @Test
    @Order(8)
    public void testSendMessageWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test send message when logged out", RequestMethodEnum.POST, "/message", 401,
                "{\"error\":\"User not logged in\"}",
                "{\"content\":\"Hello Bob this is Alice.\",\"sender_username\":\"Alice\",\"receiver_id\":2,\"receiver_username\":\"Bob\",\"chat_id\":1}");
    }

    @Test
    @Order(9)
    public void testGetMessagesFromChatWhenLoggedOut()
    {
        sendRequestAndAssertResult("Test get messages from chat", RequestMethodEnum.GET, "/message/1", 401,
                "{\"error\":\"User not logged in\"}");
    }
}
