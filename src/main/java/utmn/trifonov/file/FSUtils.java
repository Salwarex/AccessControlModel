package utmn.trifonov.file;

import utmn.trifonov.Logger;
import utmn.trifonov.Main;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSUtils {
    private static Path APP_BASE;

    public static Path getApplicationBasePath() {
        if(APP_BASE != null) return APP_BASE;
        try {
            var codeSource = Main.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URI location = codeSource.getLocation().toURI();
                Path path = Paths.get(location);

                if (Files.isRegularFile(path)) {
                    APP_BASE = path.getParent();
                }
                else if (Files.isDirectory(path)) {
                    APP_BASE = path;
                }
                return APP_BASE;
            }
        } catch (URISyntaxException e) {
            Logger.log("Не удалось определить путь репозитория.");
        }

        return Paths.get(System.getProperty("user.dir")).normalize();
    }

    public static String toRelativePath(Path inputPath) {
        Path basePath = getApplicationBasePath().normalize();
        Path normalized = inputPath.normalize();

        if (!normalized.isAbsolute()) {
            return normalized.toString();
        }

        try {
            Path relative = basePath.relativize(normalized);
            return relative.toString().replace('\\', '/');
        } catch (IllegalArgumentException e) {
            Logger.err("Путь " + inputPath + " не находится внутри базового пути " + basePath);
            return normalized.toString().replace('\\', '/');
        }
    }

    public static Path toAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return getApplicationBasePath();
        }

        String normalized = relativePath.replace('\\', '/');
        return getApplicationBasePath().resolve(normalized).normalize();
    }
}
