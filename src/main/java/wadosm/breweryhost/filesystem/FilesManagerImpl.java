package wadosm.breweryhost.filesystem;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

@Component
public class FilesManagerImpl implements FilesManager {

    @Override
    public boolean isFileAccessible(String filePath) {
        File file = new File(filePath);
        return file.canWrite();
    }

    @Override
    public boolean moveFile(String source, String destination) {
        boolean result = copyFile(source, destination);
        if (result) {
            try {
                Files.delete(Path.of(source));
            } catch (IOException e) {
            }
        }
        return result;
    }

    private void copyContent(File a, File b) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(a));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(b));

        try {
            byte[] content = in.readAllBytes();
            out.write(content);
        } finally {
            in.close();
            out.close();
        }
    }

    @Override
    public List<String> listFiles(String dirPath, String basename) {
        return listFiles(dirPath, basename, "");
    }

    @Override
    public List<String> listFiles(String dirPath, String basename, String extension) {
        File[] files = new File(dirPath).listFiles();

        if (files != null) {
            Stream<String> allFiles = Stream.of(files).map(File::getAbsolutePath);

            return allFiles.filter(
                    x -> {
                        String filename = Path.of(x).getFileName().toString();
                        return filename.startsWith(basename) && filename.endsWith(extension);
                    }
            ).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public byte[] readFile(String path) {
        try {
            File file = new File(path);

            FileInputStream reader = new FileInputStream(file);
            BufferedInputStream br = new BufferedInputStream(reader);

            byte[] buffer = br.readAllBytes();
            br.close();
            reader.close();

            return buffer;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean isFileChecksumValid(String srcFile) {
        String[] dotParts = srcFile.split("\\.");
        String dstChecksum;

        if (dotParts.length > 1) {
            String extension = dotParts[dotParts.length-1];
            String filename = srcFile.substring(0, srcFile.length() - 1 - extension.length());
            dstChecksum = getChecksumFromName(filename);
        } else {
            dstChecksum = getChecksumFromName(srcFile);
        }

        if (dstChecksum == null) {
            return false;
        }

        byte[] content = readFile(srcFile);
        CRC32 crc32 = new CRC32();
        crc32.update(content);
        long value = crc32.getValue();
        String srcChecksum = Long.toHexString(value);

        return srcChecksum.equals(dstChecksum);
    }

    private String getChecksumFromName(String filename) {
        String dstChecksum = null;

        String[] parts = filename.split("-");
        if (parts.length >= 2) {
            dstChecksum = parts[parts.length - 1];
        }
        return dstChecksum;
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Files.delete(Path.of(filePath));
        } catch (IOException e) {
            return;
        }
    }

    @Override
    public boolean copyFile(String source, String destination) {
        if (!isFileAccessible(source)) {
            return false;
        }

        try {
            copyContent(new File(source), new File(destination));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void deleteFiles(List<String> files) {
        for (String file: files) {
            deleteFile(file);
        }
    }
}
