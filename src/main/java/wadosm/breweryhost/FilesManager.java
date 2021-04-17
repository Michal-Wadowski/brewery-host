package wadosm.breweryhost;

import java.nio.file.Path;

public interface FilesManager {

    boolean isFileAccessible(Path filePath);

    boolean moveFile(Path source, Path destination);

    boolean listFiles(Path dirPath);
}
