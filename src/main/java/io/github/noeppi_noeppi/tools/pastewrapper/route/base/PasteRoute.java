package io.github.noeppi_noeppi.tools.pastewrapper.route.base;

import io.github.noeppi_noeppi.tools.pastewrapper.EditKeyManager;
import io.github.noeppi_noeppi.tools.pastewrapper.PasteApi;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Function;

public abstract class PasteRoute<T> implements Route {
    
    protected final Service spark;
    protected final PasteApi api;
    protected final EditKeyManager mgr;
    private final String content;
    private final Function<T, String> resultFunc;

    protected PasteRoute(Service spark, PasteApi api, EditKeyManager mgr, String content, Function<T, String> resultFunc) {
        this.spark = spark;
        this.api = api;
        this.mgr = mgr;
        this.content = content;
        this.resultFunc = resultFunc;
    }

    @Override
    public final Object handle(Request request, Response response) throws Exception {
        try {
            String result = this.resultFunc.apply(this.apply(request, response));
            response.status(result == null ? 204 : 200);
            if (result != null) {
                response.header("Content-Type", this.content);
            }
            return result;
        } catch (FileNotFoundException e) {
            throw this.spark.halt(404, "Not Found");
        }
    }
    
    protected abstract T apply(Request request, Response response) throws IOException;
    
    protected final String param(Request request, String key) {
        String value = request.params(":" + key);
        if (value == null) {
            throw this.spark.halt(500, "Missing key: " + key);
        }
        return value;
    }
}
