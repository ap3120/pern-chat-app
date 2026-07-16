package chatapp.models;

/*
Body of POST /message
 */
public class AddMessageRequestBody
{
    public String content;
    public String sender_id;
    public String sender_username;
    public String receiver_id;
    public String receiver_username;
    public String chat_id;
}
