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

public record IndexFileInfo(@NotNull URI uri, @NotNull Path path, @NotNull String sha1, @NotNull String sha512, int fileSize, @Nullable Type type) {
    public static @NotNull IndexFileInfo fromJson(final @NotNull JsonObject obj)
            throws NullPointerException, URISyntaxException, ClassCastException {
        final var hashes = obj.getAsJsonObject("hashes");
        final var path = Path.of(obj.getAsJsonPrimitive("path").getAsString());
        return new IndexFileInfo(
                new URI(obj.getAsJsonArray("downloads").get(0).getAsString()),
                path,
                hashes.getAsJsonPrimitive("sha1").getAsString(),
                hashes.getAsJsonPrimitive("sha512").getAsString(),
                obj.getAsJsonPrimitive("fileSize").getAsInt(),
                Type.fromPath(Objects.requireNonNull(path.getParent(), "Invalid file path: " + path))
        );
    }

    public @NotNull JsonObject toJson() {
        final var obj = new JsonObject();
        obj.addProperty("url", uri.toString());
        obj.addProperty("path", path.toString());
        final var hashes = new JsonObject();
        hashes.addProperty("sha1", sha1);
        hashes.addProperty("sha512", sha512);
        obj.add("hashes", hashes);
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
        return ModrinthHandler.fromModrinthFileToIndex(type.dir, post);
    }
}
