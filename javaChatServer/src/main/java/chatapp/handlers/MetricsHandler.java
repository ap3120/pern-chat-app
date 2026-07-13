package chatapp.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.prometheus.metrics.core.metrics.Counter;

import java.io.IOException;

public class MetricsHandler implements HttpHandler
{
    private final HttpHandler delegate;
    private final Counter requestCounter;

    public MetricsHandler(HttpHandler delegate, Counter requestCounter)
    {
        this.delegate = delegate;
        this.requestCounter = requestCounter;
    }

    @Override
    public void handle(HttpExchange httpExchange)
    {
        try
        {
            delegate.handle(httpExchange);
            requestCounter.labelValues(
                    httpExchange.getRequestMethod(),
                    "200"
            ).inc();
        } catch (IOException e)
        {
            System.out.println("Error from server: " + e.getMessage());
            requestCounter.labelValues(
                    httpExchange.getRequestMethod(),
                    "500"
            ).inc();
        }
    }
}
