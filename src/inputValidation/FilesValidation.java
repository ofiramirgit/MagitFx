package inputValidation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesValidation {
    public boolean isRepository(File selectedFolder) {
        Path filePath = Paths.get(selectedFolder.toString()+ File.separator +".magit");
        return Files.exists(filePath);
    }
}
