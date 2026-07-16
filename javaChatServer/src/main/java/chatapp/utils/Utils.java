package chatapp.utils;

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
}
