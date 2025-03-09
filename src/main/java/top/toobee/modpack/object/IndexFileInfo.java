package top.toobee.modpack.object;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.toobee.modpack.ModrinthHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

public record IndexFileInfo(
        @NotNull URI uri, @NotNull Path path, @NotNull String sha1, @NotNull String sha512,
        int fileSize, @Nullable Type type, boolean optional) {
    public static @NotNull IndexFileInfo fromJson(final @NotNull JsonObject obj)
            throws NullPointerException, URISyntaxException, ClassCastException {
        final var hashes = obj.getAsJsonObject("hashes");
        final var path = Path.of(obj.getAsJsonPrimitive("path").getAsString());

        var optional = false;
        final var env = obj.has("env") ? obj.getAsJsonObject("env") : null;
        if (env != null) {
            final var client = env.get("client");
            if (client.getAsJsonPrimitive().getAsString().equals("optional"))
                optional = true;
        }

        return new IndexFileInfo(
                new URI(obj.getAsJsonArray("downloads").get(0).getAsString()),
                path,
                hashes.getAsJsonPrimitive("sha1").getAsString(),
                hashes.getAsJsonPrimitive("sha512").getAsString(),
                obj.getAsJsonPrimitive("fileSize").getAsInt(),
                Type.fromPath(Objects.requireNonNull(path.getParent(), "Invalid file path: " + path)),
                optional
        );
    }

    public @NotNull JsonObject toJson() {
        final var obj = new JsonObject();
        final var downloads = new JsonArray();
        downloads.add(uri.toString());
        obj.add("downloads", downloads);
        obj.addProperty("path", path.toString());

        var temp = new JsonObject();
        temp.addProperty("sha1", sha1);
        temp.addProperty("sha512", sha512);
        obj.add("hashes", temp);

        temp = new JsonObject();
        temp.addProperty("client", optional ? "optional" : "required");
        obj.add("env", temp);

        obj.addProperty("fileSize", fileSize);
        return obj;
    }

    public @NotNull String getHostName() throws NullPointerException {
        return Objects.requireNonNull(uri.getHost(), "Invalid host name of url " + uri );
    }

    public @NotNull JsonObject fromModrinthFileToIndex(@NotNull JsonObject post)
            throws NullPointerException, ClassCastException {
        if (type == null)
            throw new NullPointerException("type is null");
        return ModrinthHandler.fromModrinthFileToIndex(type.dir, optional, post);
    }
}
