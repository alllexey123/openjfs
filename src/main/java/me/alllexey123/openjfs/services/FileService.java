package me.alllexey123.openjfs.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@Getter
public class FileService {

    @Value("${openjfs.datapath}")
    private String dataPathString;

    public Path getDataPath() {
        return Path.of(dataPathString);
    }
}
