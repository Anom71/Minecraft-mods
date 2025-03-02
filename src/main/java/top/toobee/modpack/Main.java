package top.toobee.modpack;

import org.jetbrains.annotations.NotNull;
import top.toobee.modpack.io.BufferedJsonWriter;
import top.toobee.modpack.io.InstantJsonReader;
import top.toobee.modpack.object.IndexFileInfo;
import top.toobee.modpack.object.Type;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;
import java.util.logging.Logger;


public class Main {
    public static final Logger LOGGER = Logger.getGlobal();

    public static void main(String[] args) {
        if (args.length != 0) {
            try {
                Main.class.getMethod(args[0]).invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void makeOut() throws Exception {
        var dir = new File("out");
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory " + dir);
    }

    public static void update() throws Exception {
        makeOut();
        var array = new InstantJsonReader("opengl","files").get().getAsJsonArray();
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
        new ModrinthHandler(new String[] {
                "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21", "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"
        }, modrMap).runUpdate();
    }

    public static void clean() throws Exception {
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
        var sc = new Scanner(System.in);
        Map<String, @NotNull Path> map = new HashMap<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.isBlank())
                break;
            String[] arr = line.split("\\s+");
            map.put(arr[0], arr.length == 1 ? Type.MOD.dir : Path.of(arr[1]));
        }
        sc.close();
        if (map.isEmpty())
            return;
        System.out.println("Starting...");
        makeOut();
        ModrinthHandler.getVersions(map);
    }

    public static void main0(String[] args) throws Exception{
        //System.out.println("Hello, World!");
        /*if (args.length != 0)
            System.out.println(args[0]);
        System.out.println("ok");*/

        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String s =gson.toJson(jsonObject1);
        fileWriter(s,"D:/nn/world/tmp/modrinth.json");*/
    }

    /*private static void fileWriter(String s,String fileName){
        try(FileWriter writer = new FileWriter(fileName)){
            writer.write(s);
            System.out.println(("file written successfully!"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }*/

    /*private static JsonObject jsonFileReader(File file)throws IOException{
        try (FileReader reader = new FileReader(file)){
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }*/

    /*private static String getUrl() throws IOException {
        JsonObject jsonObject=jsonFileReader();
        String hash=jsonObject.getAsJsonArray("files")
                .get(0).getAsJsonObject().getAsJsonObject("hashes").get("sha512").getAsString();
        return hash;
    }*/
}