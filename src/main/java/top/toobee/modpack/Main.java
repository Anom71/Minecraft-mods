package top.toobee.modpack;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import top.toobee.modpack.io.BufferedJsonWriter;
import top.toobee.modpack.io.InstantJsonReader;
import top.toobee.modpack.object.IndexFileInfo;
import top.toobee.modpack.object.Type;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;


public class Main {
    public static final Logger LOGGER = Logger.getGlobal();
    private static final String[] GATHER_FILES = new String[]{"update", "no_update", "not_modrinth", "get", "download"};

    public static void main(String[] args) {
        if (args.length != 0) {
            try {
                Main.class.getMethod(args[0]).invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void ensureDir(String name) throws IOException {
        var dir = new File(name);
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory " + dir);
    }

    public static void update() throws Exception {
        ensureDir("out");
        String graphics;
        try (var sc = new Scanner(System.in)) {
            graphics = sc.next();
        }
        var array = new InstantJsonReader(graphics).get("files").getAsJsonArray();
        Map<String, IndexFileInfo> modrMap = new HashMap<>();
        var notModrinth = new BufferedJsonWriter("not_modrinth");
        var error = new BufferedJsonWriter("error1");
        IndexFileInfo info;

        for (var element : array) {
            try {
                info = IndexFileInfo.fromJson(element.getAsJsonObject());
                if (info.getHostName().equals("cdn.modrinth.com"))
                    modrMap.put(info.sha1(), info);
                else
                    notModrinth.add(element);
            } catch (Exception e) {
                error.add(element);
                LOGGER.warning(e.getMessage());
            }
        }

        notModrinth.flush();
        error.flush();
        new ModrinthHandler(new String[]{
                "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21", "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"
        }, modrMap).runUpdate();
    }

    public static void clean() throws IOException {
        try (var stream = Files.walk(Path.of("out"))) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOGGER.warning(e.getMessage());
                        }
                    });
        }
    }

    public static void get() throws Exception {
        System.out.println("version id | dir (default: mods)");
        var map = readMapFromInput();
        if (map.isEmpty())
            return;
        ensureDir("out");
        System.out.println("Starting...");
        ModrinthHandler.getVersions(map);
    }

    public static void download() throws IOException {
        System.out.println("url | dir (default: mods)");
        var map = readMapFromInput();
        if (map.isEmpty())
            return;
        ensureDir("downloads");
        System.out.println("Starting...");

        final var download = new BufferedJsonWriter("download");
        for (var entry : map.entrySet()) {
            try {
                final var uri = new URI(entry.getKey());
                final var pair = entry.getValue();
                final var info = new DownloadHandler(uri, pair.getKey(), pair.getValue()).run();
                download.add(info.toJson());
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }
        }

        download.flush();
    }

    public static void gather() throws Exception {
        if (!new File("out").isDirectory())
            throw new IOException("Not a directory: out");
        final var array = new JsonArray();
        InstantJsonReader reader;
        for (var file : GATHER_FILES) {
            reader = new InstantJsonReader("out/" + file);
            if (reader.exists())
                array.addAll(reader.read().getAsJsonArray());
        }
        final var obj = new InstantJsonReader("meta").read().getAsJsonObject();

        var s = obj.getAsJsonPrimitive("name").getAsString();
        try (var sc = new Scanner(System.in)) {
            s = s.formatted(sc.next());
        }
        obj.addProperty("name", s);
        obj.addProperty("game", "minecraft");
        obj.add("files", array);
        new BufferedJsonWriter("modrinth.index").write(obj);
        System.out.println("Done");
    }

    private static Map<String, Map.Entry<@NotNull Path, @NotNull Boolean>> readMapFromInput() {
        final Map<String, Map.Entry<@NotNull Path, @NotNull Boolean>> map = new HashMap<>();
        try (var sc = new Scanner(System.in)) {
            String line;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.isBlank()) break;
                String[] arr = line.split("\\s+");
                map.put(arr[0], new AbstractMap.SimpleImmutableEntry<>(
                        arr.length == 2 ? Path.of(arr[1]) : Type.MOD.dir,
                        arr.length == 3 && arr[2].equals("optional")
                ));
            }
        }
        return map;
    }
}