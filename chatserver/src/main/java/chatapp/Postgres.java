package chatapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Postgres {
    /**
     * Connects to the database
     * @param dbname
     * @param user
     * @param password
     * @return
     */
    public Connection connectToDatabase(String dbname, String user, String password) {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbname, user, password);
            if (connection != null) {
                System.out.println("Connection established");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Adds a chat
     * @param conn
     * @param created_by
     * @param created_at
     */
    public void addToChats(Connection conn, int created_by, LocalDateTime created_at) {
        Statement statement;
        try {
            String query = String.format("Insert into chats (created_by, created_at) values ('%s', '%s');", created_by, created_at);
            statement = conn.createStatement();
            statement.executeUpdate(query);
            System.out.println("Row was added");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads from chats
     * @param conn
     * @param id
     */
    public void readFromChats(Connection conn, int id) {
        Statement statement;
        ResultSet rs = null;
        try {
            String query = String.format("select * from chats join users_chats on chats.chat_id = users_chats.chat_id where users_chats.user_id = '%s';", id);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                System.out.print(rs.getString("chat_id") + " ");
                System.out.print(rs.getString("created_at") + " ");
                System.out.print(rs.getString("created_by") + " ");
                System.out.println(rs.getString("user_id") + " ");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
