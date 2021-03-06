package org.moddingx.pastewrapper.route;

import org.moddingx.pastewrapper.EditKeyManager;
import org.moddingx.pastewrapper.PasteApi;
import org.moddingx.pastewrapper.route.base.TextRoute;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DeleteRoute extends TextRoute {
    
    public DeleteRoute(Service spark, PasteApi api, EditKeyManager mgr) {
        super(spark, api, mgr);
    }

    @Override
    protected String apply(Request request, Response response) throws IOException {
        String editKey = this.param(request, "key");
        String id;
        try {
            id = this.mgr.getPasteId(editKey);
        } catch (IOException e) {
            throw new FileNotFoundException();
        }
        try {
            this.api.delete(id);
        } catch (IOException e) {
            throw this.spark.halt(500, "Failed to delete paste");
        }
        return "Deleted";
    }
}
