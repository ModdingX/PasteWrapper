package org.moddingx.pastewrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DebugStart {
    
    public static void main(String[] args) throws IOException {
        String token = Files.readString(Paths.get("paste_token")).strip();
        Main.main(new String[]{"--token", token, "--no-ssl", "--port", "8080", "--private-key", "paste_private_key", "--public-key", "paste_public_key"});
    }
}
