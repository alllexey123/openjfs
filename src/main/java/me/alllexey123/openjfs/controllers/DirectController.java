package me.alllexey123.openjfs.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.services.FileService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Path;

@RestController
@RequestMapping("/direct")
@RequiredArgsConstructor
public class DirectController {

    private static final Log log = LogFactory.getLog(DirectController.class);
    private final FileService fileService;

    @GetMapping(value = "/**")
    public ResponseEntity<InputStreamResource> direct(HttpServletRequest request, HttpServletResponse response) {
        String localPath = request.getRequestURI().substring("/direct/".length());
        Path basePath = fileService.getDataPath();
        Path requestedPath = basePath.resolve(localPath).normalize();

        // locked inside our data dir
        if (!requestedPath.startsWith(basePath)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = requestedPath.toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        try {
            InputStream is = new FileInputStream(file);
            return ResponseEntity.ok(new InputStreamResource(is));
        } catch (IOException e) {
            log.info("Error writing file to output stream. Filename: " + file.getName());
            throw new RuntimeException("IOError writing file to output stream");
        }
    }
}
