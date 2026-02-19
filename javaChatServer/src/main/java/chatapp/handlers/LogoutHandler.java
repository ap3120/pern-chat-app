package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;

public class LogoutHandler extends AbstractHandler implements HttpHandler
{

    public LogoutHandler(Connection connection)
    {
        super(connection);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {

    }
}
