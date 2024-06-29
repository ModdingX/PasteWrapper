package org.moddingx.pastewrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;

// openssl genrsa -out key.pem 512
// openssl pkcs8 -topk8 -inform PEM -outform DER -in key.pem -out private_key.der -nocrypt
// openssl rsa -in key.pem -pubout -outform DER -out public_key.der
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws IOException {
        String version = null;
        try(InputStream in = Main.class.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (in != null) {
                Manifest manifest = new Manifest(in);
                version = manifest.getMainAttributes().getValue("Implementation-Version");
            }
        } catch (Exception e) {
            //
        }
        
        OptionParser options = new OptionParser(false);
        OptionSpec<Void> specDocker = options.accepts("docker", "Run in Docker mode. This will load secrets as docker secrets.");

        OptionSpec<String> specToken = options.accepts("token", "The paste.ee API token to use")
                .availableUnless(specDocker).requiredUnless(specDocker).withRequiredArg();

        OptionSpec<Path> specPubKey = options.accepts("public-key", "The public RSA key for managing paste deletion.")
                .availableUnless(specDocker).requiredUnless(specDocker).withRequiredArg()
                .withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE));

        OptionSpec<Path> specPrivKey = options.accepts("private-key", "The private RSA key for managing paste deletion.")
                .availableUnless(specDocker).requiredUnless(specDocker).withRequiredArg()
                .withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE));

        OptionSpec<Void> specNoSsl = options.accepts("no-ssl", "Disable SSL. For testing only.");

        OptionSpec<Path> specSsl = options.accepts("ssl", "The SSL certificate fle to use.")
                .availableUnless(specDocker, specNoSsl).withRequiredArg()
                .withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING, PathProperties.READABLE));

        OptionSpec<String> specSslKey = options.accepts("ssl-key", "The password of the SSL certificate.")
                .availableUnless(specDocker, specNoSsl).withRequiredArg().defaultsTo("");

        OptionSpec<Integer> specPort = options.accepts("port", "The port to run on.").withRequiredArg().ofType(Integer.class);

        OptionSet set = options.parse(args);
        
        if (version == null) {
            logger.warn("Failed to detect version, falling back to 'UNKNOWN'");
            version = "UNKNOWN";
        } else {
            logger.info("Running PasteWrapper v" + version);
        }

        boolean docker = set.has(specDocker);
        boolean useSsl = !set.has(specNoSsl);

        String token;
        Path publicKey;
        Path privateKey;
        PasteServer.SslData ssl = null;
        int port = set.has(specPort) ? set.valueOf(specPort) : (useSsl ? 443 : 80);

        if (docker) {
            token = dockerSecret("paste_token");
            publicKey = dockerSecretPath("paste_public_key");
            privateKey = dockerSecretPath("paste_private_key");
            if (useSsl) {
                ssl = new PasteServer.SslData(dockerSecret("ssl_keystore_password"), dockerSecretPath("ssl_keystore"));
            }
        } else {
            token = set.valueOf(specToken);
            publicKey = set.valueOf(specPubKey);
            privateKey = set.valueOf(specPrivKey);
            if (useSsl) {
                ssl = new PasteServer.SslData(set.valueOf(specSslKey), set.valueOf(specSsl).toAbsolutePath().normalize());
            }
        }

        PasteApi api = new PasteApi(token);
        EditKeyManager mgr = EditKeyManager.create(publicKey, privateKey);
        PasteServer server = new PasteServer(version, port, ssl, api, mgr);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        logger.info("Initialisation done.");
    }

    private static String dockerSecret(String id) throws IOException {
        return Files.readString(dockerSecretPath(id));
    }
    
    private static Path dockerSecretPath(String id) {
        return Paths.get("/", "run", "secrets", id);
    }
}
