package chatapp.dto;

/**
 * Body of POST /chat. The frontend sends created_by as a string (from
 * sessionStorage) and a created_at the server ignores (it stamps its own).
 */
public class CreateChatRequest
{
    public String created_by;
    public String created_at; // accepted but ignored, kept so deserialization does not fail
}
