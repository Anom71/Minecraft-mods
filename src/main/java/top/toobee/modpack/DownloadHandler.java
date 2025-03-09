package top.toobee.modpack;

import org.jetbrains.annotations.NotNull;
import top.toobee.modpack.object.IndexFileInfo;
import top.toobee.modpack.object.Type;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class DownloadHandler {
    private final URI uri;
    private final String fileName;
    private final Type type;
    private final boolean optional;

    public DownloadHandler(URI uri, Path dir, boolean optional) {
        this.uri = uri;
        this.fileName = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        this.type = Type.fromPath(Objects.requireNonNull(dir, "Invalid file path: " + dir.toString()));
        this.optional = optional;
    }

    public @NotNull IndexFileInfo run() throws IOException, NoSuchAlgorithmException {
        try (final InputStream in = new BufferedInputStream(uri.toURL().openStream());
             final OutputStream out = new FileOutputStream("downloads/" + fileName)) {
            final var sha1Digest = MessageDigest.getInstance("SHA-1");
            final var sha512Digest = MessageDigest.getInstance("SHA-512");
            final var sha1Stream = new DigestInputStream(in, sha1Digest);
            final var sha512Stream = new DigestInputStream(sha1Stream, sha512Digest);
            final var buffer = new byte[8192];

            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = sha512Stream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            final var f = HexFormat.of();
            return new IndexFileInfo(
                    uri,
                    type.dir.resolve(fileName),
                    f.formatHex(sha1Digest.digest()),
                    f.formatHex(sha512Digest.digest()),
                    totalBytes,
                    type,
                    optional
            );
        }
    }
}
