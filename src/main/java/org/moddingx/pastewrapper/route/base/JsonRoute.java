package org.moddingx.pastewrapper.route.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.moddingx.pastewrapper.EditKeyManager;
import org.moddingx.pastewrapper.PasteApi;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.IOException;

public abstract class JsonRoute extends PasteRoute<JsonElement> {

    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }

    protected JsonRoute(Service spark, PasteApi api, EditKeyManager mgr) {
        super(spark, api, mgr, "application/json", GSON::toJson);
    }

    @Override
    protected abstract JsonElement apply(Request request, Response response) throws IOException;
}
