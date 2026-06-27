package chatapp.dto;

/**
 * Body of POST /message. Mirrors the exact mixed types the frontend sends:
 * sender_id arrives as a string, receiver_id and chat_id as JSON numbers, and
 * send_at is accepted but ignored (the server stamps its own).
 */
public class CreateMessageRequest
{
    public String content;
    public String sender_id;
    public String sender_username;
    public Integer receiver_id;
    public String receiver_username;
    public Integer chat_id;
    public String send_at; // accepted but ignored
}
