package chatapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + dbname, user,
                    password);
        } catch (Exception e)
        {
            System.out.println("Error creating database connection: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Add a chat
     *
     * @param conn          the connection object
     * @param created_by    the chat creator
     * @param other_user_id the other chat participant
     * @return added chat as a map
     */
    public static Map<String, Object> createChat(Connection conn, int created_by, int other_user_id) throws SQLException
    {
        if (created_by == other_user_id)
        {
            return Map.of("error", "A chat requires two different users"/*, "statusCode", 409*/);
        }
        boolean previousAutoCommit = conn.getAutoCommit();
        try
        {
            conn.setAutoCommit(false);
            int user1 = Math.min(created_by, other_user_id);
            int user2 = Math.max(created_by, other_user_id);
            String findQuery = """
                    SELECT c.chat_id
                    FROM chats c
                    JOIN direct_chats dc ON dc.chat_id = c.chat_id
                    WHERE dc.user_1 = ?
                      AND dc.user_2 = ?
                    """;
            try (PreparedStatement statement = conn.prepareStatement(findQuery))
            {
                statement.setInt(1, user1);
                statement.setInt(2, user2);
                try (ResultSet rs = statement.executeQuery())
                {
                    if (rs.next())
                    {
                        conn.rollback();
                        return Map.of("error", "Chat already exists"/*, "statusCode", 409*/);
                    }
                }
            }
            LocalDateTime created_at = LocalDateTime.now();
            String createQuery = """
                    INSERT INTO chats (created_by, created_at)
                    VALUES (?, ?)
                    RETURNING *;
                    """;
            int chatId;
            List<Map<String, Object>> result;
            try (PreparedStatement statement = conn.prepareStatement(createQuery))
            {
                statement.setInt(1, created_by);
                statement.setTimestamp(2, Timestamp.valueOf(created_at));
                try (ResultSet rs = statement.executeQuery())
                {
                    result = parseResultSetToMapList(rs);
                    if (result.isEmpty())
                    {
                        return Map.of("error", "Chat creation returned no chat_id"/*, "statusCode", 400*/);
                    }
                    chatId = (int) result.get(0).get("chat_id");

                }
            }
            String directQuery = """
                    INSERT INTO direct_chats (chat_id, user_1, user_2)
                    VALUES (?, ?, ?)
                    """;
            try (PreparedStatement statement = conn.prepareStatement(directQuery))
            {
                statement.setInt(1, chatId);
                statement.setInt(2, user1);
                statement.setInt(3, user2);
                statement.executeUpdate();
            }
            String participantQuery = """
                    INSERT INTO users_chats (user_id, chat_id)
                    VALUES (?, ?)
                    """;
            try (PreparedStatement statement = conn.prepareStatement(participantQuery))
            {
                statement.setInt(1, created_by);
                statement.setInt(2, chatId);
                statement.addBatch();

                statement.setInt(1, other_user_id);
                statement.setInt(2, chatId);
                statement.addBatch();

                statement.executeBatch();
            }
            conn.commit();
            return result.get(0);
        } catch (SQLException | RuntimeException e)
        {
            System.out.println("There was an error creating the chat " + e.getMessage());
            try
            {
                conn.rollback();
            } catch (SQLException rollbackError)
            {
                e.addSuppressed(rollbackError);
            }
            return Map.of("error", "There was an error creating the chat"/*, "statusCode", 400*/);
        } finally
        {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Get all the chats of a user
     *
     * @param conn
     * @param userId
     * @return elements from chats table
     */
    public static List<Map<String, Object>> getChatsFromUser(Connection conn, int userId)
    {
        String query = """
        	select
                uc1.chat_id,
                u.user_id,
                u.username,
                c.created_at,
                c.created_by
                from users_chats as uc1
               	join users_chats as uc2 on uc1.chat_id = uc2.chat_id
                join users as u on u.user_id = uc2.user_id
                join chats as c on c.chat_id = uc1.chat_id
                where uc1.user_id = ? and uc2.user_id <> ?;
                """;
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            ResultSet rs = statement.executeQuery();
            return parseResultSetToMapList(rs);
        } catch (Exception e)
        {
            return List.of(Map.of("error", "Could not read chats from database: " + e.getMessage()));
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
    public static Map<String, Object> addUser(Connection conn, String username, String password)
    {
        String query = "insert into users (username, password) values (?, ?) returning *;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            if (userExists(conn, username))
            {
                return Map.of("error", "User " + username + " already exists.");
            }
            String hashedPassword = hash(password);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet rs = statement.executeQuery();
            Map<String, Object> result = parseResultSetToMapList(rs).get(0);
            result.remove("password");
            return result;
        } catch (Exception e)
        {
            return Map.of("error", "Something went wrong with the server.");
        }
    }

    /**
     * Get all users
     *
     * @param conn the connection object
     * @return all users
     */
    public static List<Map<String, Object>> getUsers(Connection conn)
    {
        String query = "Select user_id, username from users order by user_id asc;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            ResultSet rs = statement.executeQuery();
            return parseResultSetToMapList(rs);
        } catch (Exception e)
        {
            return List.of(Map.of("error", "Could not get users: " + e.getMessage()));
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
    public static Map<String, Object> updateUser(Connection conn, int user_id, String prevPasswordToCheck,
                                                 String newPassword)
    {
        String query1 = "Select password from users where user_id = ?;";
        try (PreparedStatement statement1 = conn.prepareStatement(query1, ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_READ_ONLY))
        {
            statement1.setInt(1, user_id);
            ResultSet rs = statement1.executeQuery();
            if (!rs.first())
            {
                return Map.of("error", "User of user_id " + user_id + " doesn't exist.");
            }
            rs.beforeFirst();
            List<Map<String, Object>> res = parseResultSetToMapList(rs);
            Map<String, Object> result = res.get(0);
            if (result.get("password").equals(hash(prevPasswordToCheck)))
            {
                String hashedNewPassword = hash(newPassword);
                String query2 = "update users set password = ? where user_id = ? returning *;";
                try (PreparedStatement statement2 = conn.prepareStatement(query2))
                {
                    statement2.setString(1, hashedNewPassword);
                    statement2.setInt(2, user_id);
                    ResultSet newRs = statement2.executeQuery();
                    if (newRs != null)
                    {
                        return Map.of("msg", "Password successfully updated");
                    } else
                    {
                        return Map.of("error", "Something went wrong, password couldn't be updated.");
                    }
                }
            } else
            {
                return Map.of("error", "Incorrect password.");
            }
        } catch (Exception e)
        {
            return Map.of("error", "Error changing password: " + e.getMessage());
        }
    }

    /**
     * Delete a user
     *
     * @param conn
     * @param user_id
     * @return info message
     */
    public static Map<String, Object> deleteUser(Connection conn, int user_id)
    {
        if (!userExists(conn, user_id))
        {
            return Map.of("error", "User with user_id " + user_id + " does not exist.");
        }
        String query = "delete from users where user_id = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, user_id);
            statement.executeUpdate();
            return Map.of("msg", "User successfully deleted.");
        } catch (Exception e)
        {
            return Map.of("error", "Error deleting user: " + e.getMessage());
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
    public static Map<String, Object> addMessage(Connection conn, String content, int sender_id, String sender_username,
                                                 int receiver_id, String receiver_username, int chat_id)
    {
        String query = "insert into messages (content, sender_id, sender_username, receiver_id, receiver_username, chat_id, send_at) values(?, ?, ?, ?, ?, ?, ?) returning *;";
        LocalDateTime send_at = LocalDateTime.now();
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, content);
            statement.setInt(2, sender_id);
            statement.setString(3, sender_username);
            statement.setInt(4, receiver_id);
            statement.setString(5, receiver_username);
            statement.setInt(6, chat_id);
            statement.setTimestamp(7, Timestamp.valueOf(send_at));
            ResultSet rs = statement.executeQuery();
            List<Map<String, Object>> result = parseResultSetToMapList(rs);
            return result.get(0);
        } catch (Exception e)
        {
            return Map.of("error", "Error inserting message: " + e.getMessage());
        }
    }

    /**
     * Get messages from a chat
     *
     * @param conn
     * @param chat_id
     * @return all messages from a chat
     */
    public static List<Map<String, Object>> getMessagesFromChat(Connection conn, int chat_id)
    {
        String query = "select * from messages where chat_id = ? order by send_at";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, chat_id);
            ResultSet rs = statement.executeQuery();
            return parseResultSetToMapList(rs);
        } catch (Exception e)
        {
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if a user exists by username
     *
     * @param conn
     * @param username
     * @return true if the user exists, else false
     */
    public static boolean userExists(Connection conn, String username)
    {
        String query = "select * from users where username = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Check if a user exists by user_id
     *
     * @param conn
     * @param user_id
     * @return true if the user exists, else false
     */
    public static boolean userExists(Connection conn, int user_id)
    {
        String query = "select * from users where user_id = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, user_id);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static Map<String, Object> login(Connection conn, String username, String password)
    {
        String query = "select * from users where username = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            if (!userExists(conn, username))
            {
                return Map.of("error", "User " + username + " doesn't exist.");
            }
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            Map<String, Object> result = parseResultSetToMapList(rs).get(0);
            String dbPassword = result.get("password").toString();
            if (dbPassword.equals(hash(password)))
            {
                result.remove("password");
                return result;
            }
            return Map.of("error", "Wrong password for " + username);
        } catch (Exception e)
        {
            return Map.of("error", "Something went wrong with the server.");
        }
    }

    /**
     * Cleans tables of database and restart auto increment
     *
     * @param conn the connection object
     */
    public static void cleanDatabase(Connection conn)
    {
        try (Statement statement = conn.createStatement())
        {
            statement.executeUpdate("TRUNCATE TABLE users CASCADE;");
            statement.executeUpdate("ALTER SEQUENCE users_user_id_seq RESTART WITH 1;");
            statement.executeUpdate("ALTER SEQUENCE messages_message_id_seq RESTART WITH 1;");
            statement.executeUpdate("ALTER SEQUENCE chats_chat_id_seq RESTART WITH 1;");
            statement.executeUpdate("ALTER SEQUENCE direct_chats_chat_id_seq RESTART WITH 1;");
        } catch (Exception pException)
        {
            System.out.println("Error cleaning tables: " + pException.getMessage());
        }
    }

    private static String hash(String password)
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

    /**
     * Convert a result set to a json array
     *
     * @param rs the result set
     * @return list of result set
     */
    private static List<Map<String, Object>> parseResultSetToMapList(ResultSet rs)
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
                            System.out.println("Error getting column names: " + e.getMessage());
                            return "?";
                        }
                    })
                    .toList();

            List<Map<String, Object>> result = new ArrayList<>();
            while (rs.next())
            {
                Map<String, Object> row = new HashMap<>();
                colNames.forEach(cn ->
                {
                    try
                    {
                        row.put(cn, rs.getObject(cn));
                    } catch (SQLException e)
                    {
                        System.out.println("Error getting row: " + e.getMessage());
                    }
                });
                result.add(row);
            }
            return result;
        } catch (SQLException e)
        {
            System.out.println("Error parsing resultSet: " + e.getMessage());
            return List.of(Map.of("error", "Error parsing resultSet."));
        }
    }
}
