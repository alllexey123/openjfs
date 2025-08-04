package me.alllexey123.openjfs.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.configuration.MainConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/direct")
@RequiredArgsConstructor
public class DirectController {

    private final MainConfigurationProperties properties;

    @GetMapping(value = "/**")
    public ResponseEntity<StreamingResponseBody> direct(HttpServletRequest request) {
        String localPath = request.getRequestURI().substring("/direct/".length());
        Path basePath = properties.getDataPathAsPath();
        Path requestedPath = basePath.resolve(localPath).normalize();

        // 403 if outside our data dir
        if (!requestedPath.startsWith(basePath)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 404 if it does not exist
        File file = requestedPath.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 404 if hidden (and hidden files are disabled)
        if (file.isHidden() && !properties.isAllowHidden()) {
            return ResponseEntity.notFound().build();
        }

        // 400 if it's a directory (and zipping dirs is disabled)
        if (file.isDirectory() && !properties.isAllowZipDirectories()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (file.isFile()) {
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = new FileInputStream(file)) {
                    inputStream.transferTo(outputStream);
                }
            };

            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(stream);
        }

        if (file.isDirectory()) {
            StreamingResponseBody stream = zipDirectory(requestedPath);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(stream);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // should not happen
    }


    private StreamingResponseBody zipDirectory(Path dirPath) {
        return outputStream -> {
            try (
                    ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));
                    Stream<Path> walk = Files.walk(dirPath)
            ) {
                zipOut.setLevel(properties.getZipCompressionLevel());

                // skip if hidden or in hidden dir (and hidden files are disabled)
                Stream<Path> filteredWalk = properties.isAllowHidden()
                        ? walk
                        : walk.filter(path -> {
                    Path relPath = dirPath.relativize(path);
                    for (Path segment : relPath) {
                        if (segment.getFileName().toString().startsWith(".")) {
                            return false;
                        }
                    }
                    return true;
                });

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
