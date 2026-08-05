package chatapp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class Utils
{
    private static final int PORT = 9230;
    private static HttpClient client = HttpClient.newHttpClient();

    public static void setClient(HttpClient client)
    {
        Utils.client = client;
    }

    public static void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                                  int pExpectedStatusCode, String pExpectedResult)
    {
        sendRequestAndAssertResult(pDescription, pMethod, pEndpoint, pExpectedStatusCode, pExpectedResult, null);
    }

    public static void sendRequestAndAssertResult(String pDescription, RequestMethodEnum pMethod, String pEndpoint,
                                                  int pExpectedStatusCode, String pExpectedResult, String pBody)
    {
        System.out.println(pDescription);
        HttpRequest request = null;
        String DOMAIN = "http://localhost:";
        switch (pMethod)
        {
            case POST:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint))
                        .POST(HttpRequest.BodyPublishers.ofString(pBody != null ? pBody : "{}"))
                        .header("Content-Type", "application/json")
                        .build();
                break;
            case GET:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).GET()
                        .header("Content-Type", "application/json").build();
                break;
            case DELETE:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint)).DELETE()
                        .header("Content-Type", "application/json").build();
                break;
            case PUT:
                request = HttpRequest.newBuilder().uri(URI.create(DOMAIN + PORT + pEndpoint))
                        .PUT(HttpRequest.BodyPublishers.ofString(pBody)).header("Content-Type", "application/json")
                        .build();
                break;
            default:
        }
        try
        {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(pExpectedStatusCode, response.statusCode());
            assertEquals(pExpectedResult, response.body());
        } catch (IOException | InterruptedException e)
        {
            fail("Error sending request: " + e.getMessage());
        }
    }
}
