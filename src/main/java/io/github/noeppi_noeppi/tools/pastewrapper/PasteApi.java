package io.github.noeppi_noeppi.tools.pastewrapper;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PasteApi {
    
    private static final Logger logger = LoggerFactory.getLogger(PasteApi.class);
    
    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        GSON = builder.create();
    }
    
    private final String token;
    private final HttpClient client;

    public PasteApi(String token) {
        this.token = token;
        this.client = HttpClient.newHttpClient();
    }

    public Paste createPaste(@Nullable String title, String content) throws IOException {
        try {
            JsonObject json = new JsonObject();
            if (title != null) json.addProperty("description", title);
            JsonArray sections = new JsonArray();
            JsonObject section = new JsonObject();
            if (title != null) section.addProperty("name", title);
            section.addProperty("contents", content);
            sections.add(section);
            json.add("sections", sections);
            String jsonStr = GSON.toJson(json) + "\n";

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonStr, StandardCharsets.UTF_8))
                    .uri(URI.create("https://api.paste.ee/v1/pastes"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Auth-Token", this.token)
                    .build();

            record Result(int code, @Nullable String data) {}
            Result result = this.client.send(request, info -> {
                if (info.statusCode() / 100 == 2 && info.statusCode() != 204) {
                    return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), str -> new Result(info.statusCode(), str));
                } else {
                    return HttpResponse.BodySubscribers.replacing(new Result(info.statusCode(), null));
                }
            }).body();

            System.out.println(result.data());
            if (result.data() == null) throw new IOException("HTTP status code " + result.code());
            try {
                JsonObject response = GSON.fromJson(result.data(), JsonObject.class);
                String id = response.get("id").getAsString();
                URI uri = URI.create(response.get("link").getAsString());
                return new Paste(id, uri);
            } catch (JsonSyntaxException | IllegalArgumentException e) {
                throw new IOException("Invalid response", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }
    
    public void delete(String pasteId) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create("https://api.paste.ee/v1/pastes/" + URLEncoder.encode(pasteId, StandardCharsets.UTF_8)))
                    .header("Accept", "application/json")
                    .header("X-Auth-Token", this.token)
                    .build();

            record Result(int code, @Nullable String data) {}
            Result result = this.client.send(request, info -> {
                if (info.statusCode() / 100 == 2 && info.statusCode() != 204) {
                    return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), str -> new Result(info.statusCode(), str));
                } else {
                    return HttpResponse.BodySubscribers.replacing(new Result(info.statusCode(), null));
                }
            }).body();
            
            if (result.data() == null) throw new IOException("HTTP status code " + result.code());
            try {
                JsonObject response = GSON.fromJson(result.data(), JsonObject.class);
                if (!response.get("success").getAsBoolean()) throw new IOException("Failed to delete paste");
            } catch (JsonSyntaxException | IllegalArgumentException e) {
                throw new IOException("Invalid response", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }
    
    public record Paste(String id, URI uri) {}
}
