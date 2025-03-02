package top.toobee.modpack.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class BufferedJsonWriter {
    private static final Logger LOGGER = Logger.getLogger("ReadJsonFromFile");

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private final @NotNull File file;
    private final @NotNull JsonArray array = new JsonArray();

    public BufferedJsonWriter(@NotNull String path) {
        this.file = new File("out/" + path + ".json");
    }

    public void write(@NotNull JsonElement element) {
        try (final var writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(element, writer);
        } catch (IOException e) {
            LOGGER.severe("Cannot write file " + file);
            throw new RuntimeException(e);
        }
    }

    public void add(@NotNull JsonElement element) {
        array.add(element);
    }

    public void flush() {
        if (!array.isEmpty())
            write(array);
    }
}
