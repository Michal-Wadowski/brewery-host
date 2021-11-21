package wadosm.breweryhost.device.filesystem;

import java.util.List;

public interface FilesManager {

    boolean isFileAccessible(String filePath);

    boolean moveFile(String source, String destination);

    List<String> listFiles(String dirPath, String basename);

    List<String> listFiles(String dirPath, String basename, String extension);

    byte[] readFile(String path);

    boolean isFileChecksumValid(String srcFile);

    void deleteFile(String filePath);

    boolean copyFile(String source, String destination);

    void deleteFiles(List<String> files);
}
