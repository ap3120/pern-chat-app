package chatapp.dto;

/** Body of POST /chat/users_chats. The frontend sends these as JSON numbers. */
public class AddUserChatRequest
{
    public Integer user_id;
    public Integer chat_id;
}
