package wadosm.breweryhost.device.filesystem;

import java.util.List;

public interface FilesManager {

    boolean isFileAccessible(String filePath);

    boolean moveFile(String source, String destination);

    List<String> listFiles(String dirPath, String basename);

    byte[] readFile(String path);

    boolean isFileChecksumValid(String srcFile);

    String getFileChecksumHex(String srcFile);

    String getChecksumHex(byte[] content);

    void deleteFile(String filePath);

    boolean copyFile(String source, String destination);

    boolean writeFile(byte[] content, String destination);

    void deleteFiles(List<String> files);
}
