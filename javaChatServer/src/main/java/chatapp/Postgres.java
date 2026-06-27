package chatapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Postgres
{
    /**
     * Connect to the database
     *
     * @param dbname   the database name
     * @param user     the username
     * @param password the user password
     * @return the connection object
     */
    public static Connection connectToDatabase(String dbname, String user, String password, String host, String port)
    {
        Connection connection = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + dbname, user, password);
        } catch (Exception e)
        {
            System.out.println("Error creating database connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Add a chat
     *
     * @param conn       the connection object
     * @param created_by the chat creator
     * @return added chat to json
     */
    public static JSONObject addToChat(Connection conn, int created_by)
    {
        boolean originalAutoCommit = true;
        try
        {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            LocalDateTime created_at = LocalDateTime.now();
            JSONObject chat;
            String chatQuery = "insert into chats (created_by, created_at) values (?, ?) returning *;";
            try (PreparedStatement statement = conn.prepareStatement(chatQuery))
            {
                statement.setInt(1, created_by);
                statement.setTimestamp(2, Timestamp.valueOf(created_at));
                try (ResultSet rs = statement.executeQuery())
                {
                    JSONArray result = convertResultSetToJson(rs);
                    chat = (JSONObject) result.get(0);
                }
            }

            int chat_id = chat.getInt("chat_id");
            String linkQuery = "insert into users_chats (user_id, chat_id) values (?, ?);";
            try (PreparedStatement linkStatement = conn.prepareStatement(linkQuery))
            {
                linkStatement.setInt(1, created_by);
                linkStatement.setInt(2, chat_id);
                linkStatement.executeUpdate();
            }

            conn.commit();
            return chat;
        } catch (Exception e)
        {
            rollbackQuietly(conn);
            e.printStackTrace();
            return null;
        } finally
        {
            restoreAutoCommit(conn, originalAutoCommit);
        }
    }

    /**
     * Add to users_chats table
     *
     * @param conn
     * @param user_id
     * @param chat_id
     * @return added row
     */
    public static JSONObject addToUsersChats(Connection conn, int user_id, int chat_id)
    {
        String query = "insert into users_chats (user_id, chat_id) values (?, ?) returning *;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, user_id);
            statement.setInt(2, chat_id);
            try (ResultSet rs = statement.executeQuery())
            {
                JSONArray result = convertResultSetToJson(rs);
                return (JSONObject) result.get(0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all the chats of a user
     *
     * @param conn
     * @param userId
     * @return elements from chats table
     */
    public static JSONArray readFromChats(Connection conn, int userId)
    {
        String query = "select * from chats join users_chats on chats.chat_id = users_chats.chat_id where users_chats.user_id = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery())
            {
                return convertResultSetToJson(rs);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the other participant in the specified chat for the given user
     *
     * @param conn
     * @param chat_id
     * @param user_id
     * @return user
     */
    public static JSONObject getUser(Connection conn, int chat_id, int user_id)
    {
        String query = "select users.user_id, users.username from users join users_chats on users.user_id = users_chats.user_id where users_chats.chat_id = ? and users_chats.user_id != ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, chat_id);
            statement.setInt(2, user_id);
            try (ResultSet rs = statement.executeQuery())
            {
                JSONArray result = convertResultSetToJson(rs);
                return (JSONObject) result.get(0);
            }
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Get all users
     *
     * @param conn the connection object
     * @return all users
     */
    public static JSONArray getUsers(Connection conn)
    {
        String query = "Select user_id, username from users order by user_id asc;";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet rs = statement.executeQuery())
        {
            return convertResultSetToJson(rs);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update a user
     *
     * @param conn
     * @param user_id
     * @param prevPasswordToCheck
     * @param newPassword
     * @return info message
     */
    public static JSONObject updateUser(Connection conn, int user_id, String prevPasswordToCheck, String newPassword)
    {
        try
        {
            String selectQuery = "Select password from users where user_id = ?;";
            String storedPassword;
            try (PreparedStatement statement = conn.prepareStatement(selectQuery))
            {
                statement.setInt(1, user_id);
                try (ResultSet rs = statement.executeQuery())
                {
                    if (!rs.next())
                    {
                        return infoMessage("User of user_id " + user_id + " doesn't exist.");
                    }
                    storedPassword = rs.getString("password");
                }
            }

            if (!passwordMatches(prevPasswordToCheck, storedPassword))
            {
                return infoMessage("Incorrect password.");
            }

            String updateQuery = "update users set password = ? where user_id = ?;";
            try (PreparedStatement updateStatement = conn.prepareStatement(updateQuery))
            {
                updateStatement.setString(1, hash(newPassword));
                updateStatement.setInt(2, user_id);
                int updated = updateStatement.executeUpdate();
                if (updated > 0) return infoMessage("Password successfully updated");
                return infoMessage("Something went wrong, password couldn't be updated.");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a user
     *
     * @param conn
     * @param user_id
     * @return info message
     */
    public static JSONObject deleteUser(Connection conn, int user_id)
    {
        String query = "delete from users where user_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, user_id);
            statement.executeUpdate();
            return infoMessage("User successfully deleted.");
        } catch (Exception e)
        {
            System.out.println("Error deleting user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Add a message
     *
     * @param conn
     * @param content
     * @param sender_id
     * @param sender_username
     * @param receiver_id
     * @param receiver_username
     * @param chat_id
     * @return the added message
     */
    public static JSONObject addMessage(Connection conn, String content, int sender_id, String sender_username,
                                        int receiver_id, String receiver_username, int chat_id)
    {
        LocalDateTime send_at = LocalDateTime.now();
        String query = "insert into messages (content, sender_id, sender_username, receiver_id, receiver_username, chat_id, send_at) values (?, ?, ?, ?, ?, ?, ?) returning *;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, content);
            statement.setInt(2, sender_id);
            statement.setString(3, sender_username);
            statement.setInt(4, receiver_id);
            statement.setString(5, receiver_username);
            statement.setInt(6, chat_id);
            statement.setTimestamp(7, Timestamp.valueOf(send_at));
            try (ResultSet rs = statement.executeQuery())
            {
                JSONArray result = convertResultSetToJson(rs);
                return (JSONObject) result.get(0);
            }
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Get messages from a chat
     *
     * @param conn
     * @param chat_id
     * @return all messages from a chat
     */
    public static JSONArray getMessagesFromChat(Connection conn, int chat_id)
    {
        String query = "select * from messages where chat_id = ? order by send_at";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, chat_id);
            try (ResultSet rs = statement.executeQuery())
            {
                return convertResultSetToJson(rs);
            }
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Check if a user exists
     *
     * @param conn
     * @param username
     * @return
     */
    public static boolean userExists(Connection conn, String username)
    {
        String query = "select 1 from users where username = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery())
            {
                return rs.next();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add a user
     *
     * @param conn
     * @param username
     * @param password
     * @return the added user
     */
    public static JSONObject addUser(Connection conn, String username, String password)
    {
        try
        {
            if (userExists(conn, username)) return infoMessage("User " + username + " already exists.");
            String hashedPassword = hash(password);
            String query = "insert into users (username, password) values (?, ?) returning *;";
            try (PreparedStatement statement = conn.prepareStatement(query))
            {
                statement.setString(1, username);
                statement.setString(2, hashedPassword);
                try (ResultSet rs = statement.executeQuery())
                {
                    JSONObject result = convertResultSetToJson(rs).getJSONObject(0);
                    result.remove("password");
                    return result;
                }
            }
        } catch (Exception e)
        {
            return infoMessage("Something went wrong with the server.");
        }
    }

    public static JSONObject login(Connection conn, String username, String password)
    {
        try
        {
            String query = "select * from users where username = ?;";
            try (PreparedStatement statement = conn.prepareStatement(query))
            {
                statement.setString(1, username);
                try (ResultSet rs = statement.executeQuery())
                {
                    JSONArray rows = convertResultSetToJson(rs);
                    if (rows.length() == 0) return infoMessage("User " + username + " doesn't exist.");
                    JSONObject result = rows.getJSONObject(0);
                    String dbPassword = result.getString("password");
                    if (!passwordMatches(password, dbPassword))
                    {
                        return infoMessage("Wrong password for " + username);
                    }
                    // Transparently upgrade legacy SHA-512 hashes to bcrypt on a successful login.
                    if (isLegacyHash(dbPassword))
                    {
                        upgradePasswordHash(conn, result.getInt("user_id"), password);
                    }
                    result.remove("password");
                    return result;
                }
            }
        } catch (Exception e)
        {
            return infoMessage("Something went wrong with the server.");
        }
    }

    /**
     * Cleans tables of database and restart auto increment
     * @param conn the connection object
     */
    public static void cleanDatabase(Connection conn) {
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate("TRUNCATE TABLE users CASCADE;");
            statement.executeUpdate("ALTER SEQUENCE users_user_id_seq RESTART WITH 1;");
            statement.executeUpdate("ALTER SEQUENCE messages_message_id_seq RESTART WITH 1;");
            statement.executeUpdate("ALTER SEQUENCE chats_chat_id_seq RESTART WITH 1;");
        } catch (Exception pException) {
            System.out.println("Error cleaning tables: " + pException.getMessage());
        }
    }

    private static JSONObject infoMessage(String msg)
    {
        JSONObject error = new JSONObject();
        error.put("msg", msg);
        return error;
    }

    /**
     * Hash a plaintext password with bcrypt (salted, work-factored).
     */
    private static String hash(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verify a plaintext password against a stored hash. Supports both new bcrypt
     * hashes and the legacy unsalted SHA-512 hex hashes so accounts created before
     * the migration keep working until they are upgraded on next login.
     */
    private static boolean passwordMatches(String plaintext, String storedHash)
    {
        if (plaintext == null || storedHash == null) return false;
        if (isLegacyHash(storedHash))
        {
            String legacy = legacySha512(plaintext);
            return legacy != null && legacy.equalsIgnoreCase(storedHash);
        }
        try
        {
            return BCrypt.checkpw(plaintext, storedHash);
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    /**
     * Legacy hashes are exactly 128 hex characters (SHA-512); bcrypt hashes start with "$2".
     */
    private static boolean isLegacyHash(String storedHash)
    {
        return storedHash != null && storedHash.matches("[0-9a-fA-F]{128}");
    }

    private static void upgradePasswordHash(Connection conn, int user_id, String plaintext)
    {
        String query = "update users set password = ? where user_id = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, hash(plaintext));
            statement.setInt(2, user_id);
            statement.executeUpdate();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String legacySha512(String password)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytePassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashedPassword = new StringBuilder();
            for (byte b : bytePassword)
            {
                hashedPassword.append(String.format("%02x", b));
            }
            return hashedPassword.toString();
        } catch (Exception e)
        {
            return null;
        }
    }

    private static void rollbackQuietly(Connection conn)
    {
        try
        {
            if (conn != null) conn.rollback();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static void restoreAutoCommit(Connection conn, boolean autoCommit)
    {
        try
        {
            if (conn != null) conn.setAutoCommit(autoCommit);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Convert a result set to a json array
     *
     * @param rs the result set
     * @return json array of result set
     */
    private static JSONArray convertResultSetToJson(ResultSet rs)
    {
        try
        {
            ResultSetMetaData md = rs.getMetaData();
            int numCols = md.getColumnCount();
            List<String> colNames = IntStream.range(0, numCols)
                    .mapToObj(i ->
                    {
                        try
                        {
                            return md.getColumnName(i + 1);
                        } catch (SQLException e)
                        {
                            e.printStackTrace();
                            return "?";
                        }
                    })
                    .collect(Collectors.toList());

            JSONArray result = new JSONArray();
            while (rs.next())
            {
                JSONObject row = new JSONObject();
                colNames.forEach(cn ->
                {
                    try
                    {
                        row.put(cn, rs.getObject(cn));
                    } catch (JSONException | SQLException e)
                    {
                        e.printStackTrace();
                    }
                });
                result.put(row);
            }
            return result;
        } catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
