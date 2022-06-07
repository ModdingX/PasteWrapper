package org.moddingx.pastewrapper.route.base;

import org.moddingx.pastewrapper.EditKeyManager;
import org.moddingx.pastewrapper.PasteApi;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.IOException;
import java.util.function.Function;

public abstract class TextRoute extends PasteRoute<String> {

    protected TextRoute(Service spark, PasteApi api, EditKeyManager mgr) {
        super(spark, api, mgr, "text/plain", Function.identity());
    }

    @Override
    protected abstract String apply(Request request, Response response) throws IOException;
}
