package me.alllexey123.openjfs.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.model.FileInfo;
import me.alllexey123.openjfs.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class ListingController {

    private final FileService fileService;

    @GetMapping(value = "/**")
    public ResponseEntity<FileInfo> direct(HttpServletRequest request) {
        String requestedPathStr = request.getRequestURI().substring("/list/".length());
        Path fullPath = fileService.getFullPath(Path.of(requestedPathStr));

        HttpStatusCode accessCheck = fileService.checkAccess(fullPath);
        if (!accessCheck.is2xxSuccessful()) return ResponseEntity.status(accessCheck).build();

        FileInfo fi = fileService.getFileInfo(fullPath, 1);

        return ResponseEntity.status(HttpStatus.OK).body(fi);
    }
}
