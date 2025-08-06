package me.alllexey123.openjfs.services;

import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.configuration.MainConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MainConfigurationProperties properties;

    public Path getFullPath(Path requestedPath) {
        return properties.getDataPathAsPath().resolve(requestedPath).normalize();
    }

    public HttpStatusCode checkAccess(Path fullPath) {
        // 403 if outside our data dir
        if (!isInsideDataDir(fullPath)) return HttpStatus.FORBIDDEN;
        // 404 if it does not exist
        if (!Files.exists(fullPath)) return HttpStatus.NOT_FOUND;
        // 404 if hidden or in hidden dir (and hidden files are disabled)
        if (isHidden(fullPath) && !properties.isAllowHidden()) return HttpStatus.NOT_FOUND;
        return HttpStatus.OK;
    }

    public boolean isInsideDataDir(Path fullPath) {
        return fullPath.startsWith(properties.getDataPathAsPath());
    }

    // check if file is hidden or in hidden directory
    public boolean isHidden(Path fullPath) {
        return isHiddenRelative(fullPath, properties.getDataPathAsPath());
    }

    private boolean isHiddenRelative(Path fullPath, Path basePath) {
        Path relPath = basePath.relativize(fullPath);
        for (Path segment : relPath) {
            if (isHiddenByName(segment)) {
                return true;
            }
        }
        return false;
    }

    public boolean isHiddenByName(Path filePath) {
        return filePath.getFileName().toString().startsWith(".");
    }

    public StreamingResponseBody zipDirectory(Path dirPath) {
        return outputStream -> {
            try (
                    ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));
                    Stream<Path> walk = Files.walk(dirPath)
            ) {
                zipOut.setLevel(properties.getZipCompressionLevel());

                // skip if hidden or in hidden dir (and hidden files are disabled)
                Stream<Path> filteredWalk = properties.isAllowHidden()
                        ? walk
                        : walk.filter(path -> isHiddenRelative(path, dirPath));

                filteredWalk.forEach(path -> {
                    try {
                        Path relPath = dirPath.relativize(path);

                        if (Files.isDirectory(path)) {
                            if (!relPath.toString().isEmpty()) {
                                ZipEntry dirEntry = new ZipEntry(relPath + "/");
                                zipOut.putNextEntry(dirEntry);
                                zipOut.closeEntry();
                            }
                        } else {
                            ZipEntry fileEntry = new ZipEntry(relPath.toString());
                            zipOut.putNextEntry(fileEntry);
                            try (InputStream in = Files.newInputStream(path)) {
                                in.transferTo(zipOut);
                            }
                            zipOut.closeEntry();
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        };
    }
}
