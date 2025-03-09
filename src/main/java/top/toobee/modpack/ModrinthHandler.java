package top.toobee.modpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import top.toobee.modpack.io.BufferedJsonWriter;
import top.toobee.modpack.object.IndexFileInfo;
import top.toobee.modpack.object.Type;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModrinthHandler {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();
    private static final JsonArray LOADERS = new JsonArray();
    private static final URI UPDATE_URI;

    static {
        Arrays.stream(Type.values())
                .map(type -> type.loader)
                .collect(Collectors.toUnmodifiableSet())
                .forEach(LOADERS::add);
        try {
            UPDATE_URI = new URI("https://api.modrinth.com/v2/version_files/update");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public final String[] mcVersions;
    private final Map<String, IndexFileInfo> originalIndexFilesMap;
    private final HttpRequest.Builder updateRequestBuilder;
    private final BufferedJsonWriter noUpdate = new BufferedJsonWriter("no_update");
    private final BufferedJsonWriter error = new BufferedJsonWriter("error2");

    public ModrinthHandler(String[] mcVersions, Map<String, IndexFileInfo> map) {
        this.mcVersions = mcVersions;
        this.originalIndexFilesMap = map;
        this.updateRequestBuilder = HttpRequest.newBuilder(UPDATE_URI)
                .header("Accept","*/*")
                .header("Content-Type","application/json")
                .timeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2);
    }

    public void runUpdate() throws IOException, InterruptedException {
        update(true);
        update(false);
        noUpdate.flush();
        error.flush();

        var noResult = new BufferedJsonWriter("no_result");
        originalIndexFilesMap.values().forEach(info -> noResult.add(info.toJson()));
        noResult.flush();
    }

    private void update(boolean firstQuery) throws IOException, InterruptedException {
        String result = post(body(firstQuery));
        JsonObject rspObj = JsonParser.parseString(result).getAsJsonObject();
        BufferedJsonWriter update = new BufferedJsonWriter(firstQuery ? "update" : "abnormal_update");

        for (var entry : rspObj.entrySet()) {
            var oldValue = originalIndexFilesMap.remove(entry.getKey());
            var newValue = extractFileInfo(entry.getValue().getAsJsonObject());
            String newHash = newValue.getAsJsonObject("hashes").get("sha1").getAsString();
            try {
                (entry.getKey().equals(newHash) ? noUpdate : update)
                        .add(oldValue.fromModrinthFileToIndex(newValue));
            } catch (Exception e) {
                error.add(oldValue.toJson());
                Main.LOGGER.warning(e.getMessage());
            }
        }
        update.flush();
    }

    private String body(boolean firstVersion) {
        final var hashes = new JsonArray();
        originalIndexFilesMap.keySet().forEach(hashes::add);

        final var versionsJson = new JsonArray();
        if (firstVersion)
            versionsJson.add(mcVersions[0]);
        else
            Arrays.stream(mcVersions).forEach(versionsJson::add);

        var obj = new JsonObject();
        obj.add("hashes", hashes);
        obj.addProperty("algorithm","sha1");
        obj.add("loaders", LOADERS);
        obj.add("game_versions", versionsJson);

        return obj.toString();
    }

    private String post(String body) throws IOException, InterruptedException {
        HttpRequest request = updateRequestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
        ).build();
        return HTTP_CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString()
        ).body();
    }

    public static @NotNull JsonObject extractFileInfo(@NotNull JsonObject versionInfo) {
        JsonArray array = versionInfo.getAsJsonObject().getAsJsonArray("files");

        if (array.size() == 1) {
            return array.get(0).getAsJsonObject();
        } else {
            JsonObject temp;
            for (var m : array) {
                temp = m.getAsJsonObject();
                if (temp.get("primary").getAsBoolean())
                    return temp;
            }
        }

        return array.get(0).getAsJsonObject();
    }

    public static @NotNull JsonObject fromModrinthFileToIndex(@NotNull Path dir, boolean optional, @NotNull JsonObject post)
            throws NullPointerException, ClassCastException {
        final var obj = new JsonObject();
        final var downloads = new JsonArray();
        downloads.add(post.getAsJsonPrimitive("url"));
        obj.add("downloads", downloads);
        obj.add("fileSize", post.getAsJsonPrimitive("size"));
        obj.add("hashes", post.getAsJsonObject("hashes"));
        obj.addProperty("path", dir.resolve(post.getAsJsonPrimitive("filename").getAsString()).toString());
        final var env = new JsonObject();
        env.addProperty("client", optional ? "optional" : "required");
        obj.add("env", env);
        return obj;
    }

    public static @NotNull URI resolveURI(Set<String> set) throws URISyntaxException {
        var array = new JsonArray();
        set.forEach(array::add);
        String encodedParams = URLEncoder.encode(array.toString(), StandardCharsets.UTF_8);
        return new URI("https://api.modrinth.com/v2/").resolve("versions?ids=" + encodedParams);
    }

    public static void getVersions(Map<String, Map.Entry<@NotNull Path, @NotNull Boolean>> map)
            throws URISyntaxException, IOException, InterruptedException {
        var request = HttpRequest.newBuilder(resolveURI(map.keySet()))
                .header("Accept","*/*")
                .timeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();
        var response = HTTP_CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString()
        ).body();

        JsonArray array = JsonParser.parseString(response).getAsJsonArray();
        var result = new BufferedJsonWriter("get");
        var noResult = new BufferedJsonWriter("no_result");
        var error = new BufferedJsonWriter("error");

        for (var element : array) {
            try {
                var obj = element.getAsJsonObject();
                var dir = map.remove(obj.getAsJsonPrimitive("id").getAsString());
                var index = fromModrinthFileToIndex(dir.getKey(), dir.getValue(), extractFileInfo(obj));
                result.add(index);
            } catch (Exception e) {
                error.add(element);
                Main.LOGGER.warning(e.getMessage());
            }
        }

        map.keySet().forEach(id -> noResult.add(new JsonPrimitive(id)));
        result.flush();
        noResult.flush();
        error.flush();
    }
}
