package org.moddingx.pastewrapper;

import org.moddingx.pastewrapper.route.DeleteRoute;
import org.moddingx.pastewrapper.route.UsePostRoute;
import org.moddingx.pastewrapper.route.VersionRoute;
import org.moddingx.pastewrapper.route.CreateRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.nio.file.Path;

public class PasteServer {

    private static final Logger logger = LoggerFactory.getLogger(PasteServer.class);

    private final String version;
    private final Service spark;

    public PasteServer(String version, int port, SslData ssl, int threads, PasteApi api, EditKeyManager mgr) {
        logger.info("Starting Server on port {}.", port);
        this.version = version;
        this.spark = Service.ignite();
        this.spark.port(port);
        logger.info("Running on {} threads.", threads);
        this.spark.threadPool(threads, threads, -1);
        if (ssl != null) {
            this.spark.secure(ssl.cert().toAbsolutePath().normalize().toString(), ssl.key(), null, null);
        } else {
            logger.warn("Running without SSL.");
        }

        // Support trailing slashes
        this.spark.before((req, res) -> {
            String path = req.pathInfo();
            if (path.length() > 1 && path.endsWith("/")) {
                res.redirect(path.substring(0, path.length() - 1));
            }
        });

        this.spark.get("/version", new VersionRoute(this.spark, api, mgr, version));
        this.spark.get("/create", new UsePostRoute(this.spark, api, mgr));
        this.spark.post("/create", new CreateRoute(this.spark, api, mgr));
        this.spark.get("/delete/:key", new DeleteRoute(this.spark, api, mgr));

        this.spark.awaitInitialization();
        logger.info("Server started.");
    }

    public String version() {
        return version;
    }

    public void shutdown() {
        this.spark.stop();
    }

    public record SslData(String key, Path cert) {}
}
