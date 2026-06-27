package chatapp.controllers;

import io.javalin.http.Context;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Shared response helpers so every controller writes JSON the same way and maps a
 * null result from the data layer to a 500 instead of an empty body.
 */
public final class Responses
{
    private Responses() {}

    public static final String JSON = "application/json";

    public static void ok(Context ctx, JSONObject body)
    {
        if (body == null)
        {
            serverError(ctx);
            return;
        }
        ctx.status(200).contentType(JSON).result(body.toString());
    }

    public static void ok(Context ctx, JSONArray body)
    {
        if (body == null)
        {
            serverError(ctx);
            return;
        }
        ctx.status(200).contentType(JSON).result(body.toString());
    }

    public static void serverError(Context ctx)
    {
        ctx.status(500).contentType(JSON)
                .result(new JSONObject().put("msg", "Something went wrong with the server.").toString());
    }
}
