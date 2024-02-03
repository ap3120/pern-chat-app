package chatapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            if (connection != null) {}
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
     * @return added chat to json
     */
    public JSONArray addToChats(Connection conn, int created_by, LocalDateTime created_at) {
        Statement statement;
        try {
            String query = String.format("insert into chats (created_by, created_at) values ('%s', '%s') returning *;", created_by, created_at);
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            String chat_id = rs.getString("chat_id");
            String second_query = String.format("insert into users_chats (user_id, chat_id) values ('%s', '%s');", created_by, chat_id);
            Statement second_statement = conn.createStatement();
            second_statement.executeUpdate(second_query);

            rs.beforeFirst();

            JSONArray result = convertResultSetToJson(rs);
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds to users_chats table
     * @param conn
     * @param user_id
     * @param chat_id
     * @return added row
     */
    public JSONArray addToUsersChats(Connection conn, int user_id, int chat_id) {
        Statement statement;
        try {
            String query = String.format("insert into users_chats (user_id, chat_id) values ('%s', '%s') returning *;", user_id, chat_id);
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(query);
            JSONArray result = convertResultSetToJson(rs);
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads from chats
     * @param conn
     * @param id
     * @return elements from chats table
     */
    public JSONArray readFromChats(Connection conn, int id) {
        Statement statement;
        try {
            String query = String.format("select * from chats join users_chats on chats.chat_id = users_chats.chat_id where users_chats.user_id = '%s';", id);
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            JSONArray result = convertResultSetToJson(rs);
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets user from a chat
     * @param conn
     * @param chat_id
     * @param user_id
     * @return user
     */
    public JSONArray getUser(Connection conn, int chat_id, int user_id) {
        Statement statement;
        try {
            String query = String.format("select users.user_id, users.username from users join users_chats on users.user_id = users_chats.user_id where users_chats.chat_id = '&s' and users_chats.user_id != '%s';", chat_id, user_id);
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            JSONArray result = convertResultSetToJson(rs);
            return result;
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Converts a result set to a json array
     * @param rs
     * @return json array of result set
     */
    private JSONArray convertResultSetToJson(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int numCols = md.getColumnCount();
            List<String> colNames = IntStream.range(0, numCols)
                    .mapToObj(i -> {
                        try {
                            return md.getColumnName(i + 1);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return "?";
                        }
                    })
                    .collect(Collectors.toList());

            JSONArray result = new JSONArray();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                colNames.forEach(cn -> {
                    try {
                        row.put(cn, rs.getObject(cn));
                    } catch (JSONException | SQLException e) {
                        e.printStackTrace();
                    }
                });
                result.put(row);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
