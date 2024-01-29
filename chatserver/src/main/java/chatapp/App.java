package chatapp;

import io.github.cdimascio.dotenv.Dotenv;

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
        LocalDateTime created_at = LocalDate.of(2024, 1, 29).atStartOfDay();
        //postgres.addToChats(connection, 6, created_at);
        postgres.readFromChats(connection, 1);
    }
}
