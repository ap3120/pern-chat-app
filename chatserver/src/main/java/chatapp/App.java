package chatapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Dotenv dotenv = Dotenv.load();
        String dbname = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        Postgres postgres = new Postgres();
        Connection connection = postgres.connectToDatabase(dbname, user, password);
        LocalDateTime created_at = LocalDate.of(2024, 1, 30).atStartOfDay();
        JSONArray result = postgres.addToChats(connection, 6, created_at);
        printJsonArray(result);
        JSONArray res = postgres.readFromChats(connection, 1);
        printJsonArray(res);
    }

    private static void printJsonArray(JSONArray jsonArray) {
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            for (String s : jo.keySet()) {
                System.out.print(s + ": " + jo.get(s) + " | ");
            }
            System.out.println();
        }
    }
}
