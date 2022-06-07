package org.moddingx.pastewrapper.route;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.moddingx.pastewrapper.EditKeyManager;
import org.moddingx.pastewrapper.PasteApi;
import org.moddingx.pastewrapper.route.base.JsonRoute;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.IOException;

public class CreateRoute extends JsonRoute {
    
    public CreateRoute(Service spark, PasteApi api, EditKeyManager mgr) {
        super(spark, api, mgr);
    }

    @Override
    protected JsonElement apply(Request request, Response response) throws IOException {
        String title = request.queryParams("title");
        String content = request.body();
        if (content == null) throw this.spark.halt(400, "No Content");
        PasteApi.Paste paste = this.api.createPaste(title, content);
        JsonObject json = new JsonObject();
        json.addProperty("url", paste.uri().toString());
        json.addProperty("edit", this.mgr.getEditToken(paste.id()));
        return json;
    }
}
