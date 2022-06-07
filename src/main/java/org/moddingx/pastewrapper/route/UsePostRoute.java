package org.moddingx.pastewrapper.route;

import org.moddingx.pastewrapper.EditKeyManager;
import org.moddingx.pastewrapper.PasteApi;
import org.moddingx.pastewrapper.route.base.TextRoute;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.IOException;

public class UsePostRoute extends TextRoute {

    public UsePostRoute(Service spark, PasteApi api, EditKeyManager mgr) {
        super(spark, api, mgr);
    }

    @Override
    protected String apply(Request request, Response response) throws IOException {
        throw this.spark.halt(405, "Use POST");
    }
}
