package io.github.noeppi_noeppi.tools.pastewrapper.route;

import io.github.noeppi_noeppi.tools.pastewrapper.EditKeyManager;
import io.github.noeppi_noeppi.tools.pastewrapper.PasteApi;
import io.github.noeppi_noeppi.tools.pastewrapper.route.base.TextRoute;
import spark.Request;
import spark.Response;
import spark.Service;

import java.io.IOException;

public class VersionRoute extends TextRoute {

    private final String version;
    
    public VersionRoute(Service spark, PasteApi api, EditKeyManager mgr, String version) {
        super(spark, api, mgr);
        this.version = version;
    }

    @Override
    protected String apply(Request request, Response response) throws IOException {
        return this.version;
    }
}
