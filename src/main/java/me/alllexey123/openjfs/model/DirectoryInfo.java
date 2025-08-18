package me.alllexey123.openjfs.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DirectoryInfo implements FileInfo {

    private String path;

    private String name;

    private LocalDateTime lastModified;

    private long lastModifiedMillis;

    private List<FileInfo> files = new ArrayList<>();

    private boolean isEmpty;

    @Override
    public FileType getType() {
        return FileType.DIRECTORY;
    }
}
