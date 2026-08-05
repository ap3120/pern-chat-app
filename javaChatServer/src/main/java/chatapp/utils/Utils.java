package chatapp.utils;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

public class Utils
{
    public static boolean isInt(String str)
    {
        if (str == null)
        {
            return false;
        }
        try
        {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static int requireLogin(Context ctx)
    {
        Integer user_id = ctx.sessionAttribute("user_id");
        if (user_id == null)
        {
            throw new UnauthorizedResponse("{\"error\":\"User not logged in\"}");
        }
        return user_id;
    }
}
