package wadosm.breweryhost.filesystem;

import java.nio.file.Path;
import java.util.List;

public interface FilesManager {

    boolean isFileAccessible(String filePath);

    boolean moveFile(String source, String destination);

    List<String> listFiles(String dirPath);

    byte[] readFile(String path);
}
