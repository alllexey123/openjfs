package me.alllexey123.openjfs.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.configuration.MainConfigurationProperties;
import me.alllexey123.openjfs.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/direct")
@RequiredArgsConstructor
public class DirectController {

    private final MainConfigurationProperties properties;

    private final FileService fileService;

    @GetMapping(value = "/**")
    public ResponseEntity<StreamingResponseBody> direct(HttpServletRequest request) throws IOException {
        String requestedPathStr = request.getRequestURI().substring("/direct/".length()).replace("%20", " ");
        Path totalPath = fileService.getFullPath(Path.of(requestedPathStr));

        HttpStatusCode accessCheck = fileService.checkAccess(totalPath);
        if (!accessCheck.is2xxSuccessful()) return ResponseEntity.status(accessCheck).build();

        String filename = totalPath.getFileName().toString();

        if (Files.isDirectory(totalPath)) {
            // 400 if it's a directory (and zipping dirs is disabled)
            if (!properties.isAllowZipDirectories()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            StreamingResponseBody zipStream = fileService.zipDirectory(totalPath);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"%s.zip\"".formatted(filename))
                    .contentType(new MediaType("application", "zip"))
                    .body(zipStream);
        }

        if (Files.isRegularFile(totalPath)) {
            StreamingResponseBody stream = outputStream -> {
                try (InputStream inputStream = Files.newInputStream(totalPath)) {
                    inputStream.transferTo(outputStream);
                }
            };

            return ResponseEntity.ok()
                    .contentLength(Files.size(totalPath))
                    .header("Content-Disposition", "attachment; filename=\"%s\"".formatted(filename))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(stream);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // should not happen
    }
}
