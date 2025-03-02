package top.toobee.modpack.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public enum Type {
    MOD(Path.of("mods"), "fabric"),
    OPTIONAL_MOD(Path.of("mods/optional"), "fabric"),
    RESOURCE_PACK(Path.of("resourcepacks"), "minecraft"),
    SHADER_PACK(Path.of("shaderpacks"), "iris");

    public final @NotNull Path dir;
    public final @NotNull String loader;

    Type(@NotNull Path dir, @NotNull String loader) {
        this.dir = dir;
        this.loader = loader;
    }

    public static @Nullable Type fromPath(@NotNull Path dir) {
        for (var type : values())
            if (type.dir.equals(dir))
                return type;
        return null;
    }
}
