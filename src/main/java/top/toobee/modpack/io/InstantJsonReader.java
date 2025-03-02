package top.toobee.modpack.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class InstantJsonReader {
    private static final Logger LOGGER = Logger.getLogger("WriteJsonToFile");

    private final @NotNull File file;
    private final @NotNull String key;
    public InstantJsonReader(@NotNull String path, @NotNull String key) {
        this.file = new File(path + ".json");
        this.key = key;
    }

    public @NotNull JsonElement read() {
        try (final var reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            LOGGER.severe("Cannot read file " + file);
            throw new RuntimeException(e);
        }
    }

    public @NotNull JsonElement get() {
        return read().getAsJsonObject().get(key);
    }
}
