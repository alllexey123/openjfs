package me.alllexey123.openjfs.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.alllexey123.openjfs.model.FileInfo;
import me.alllexey123.openjfs.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final FileService fileService;

    @GetMapping(value = "/**")
    public ResponseEntity<List<FileInfo>> search(HttpServletRequest request, @RequestParam("q") String query) {
        String requestedPathStr = request.getRequestURI().substring("/search/".length());
        Path fullPath = fileService.getFullPath(Path.of(requestedPathStr));

        HttpStatusCode accessCheck = fileService.checkAccess(fullPath);
        if (!accessCheck.is2xxSuccessful()) return ResponseEntity.status(accessCheck).build();

        List<FileInfo> result = fileService.simpleSearch(fullPath, query);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
