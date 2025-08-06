package me.alllexey123.openjfs.configuration;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "openjfs")
@Getter
@Setter
public class MainConfigurationProperties {

    private String dataPath;

    private boolean allowHidden;

    private boolean allowZipDirectories;

    @Range(min = 0, max = 9)
    private int zipCompressionLevel;

    public Path getDataPathAsPath() {
        return Path.of(dataPath);
    }
}
