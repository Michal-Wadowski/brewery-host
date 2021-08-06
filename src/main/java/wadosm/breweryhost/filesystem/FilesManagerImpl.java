package wadosm.breweryhost.filesystem;

import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FilesManagerImpl implements FilesManager {

    @Override
    public boolean isFileAccessible(String filePath) {
        File file = new File(filePath);
        return file.canWrite();
    }

    @Override
    public boolean moveFile(String source, String destination) {
        if (!isFileAccessible(source)) {
            return false;
        }

        try {
            copyContent(new File(source), new File(destination));
            Files.delete(Path.of(source));
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
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
    public List<String> listFiles(String dirPath) {
        File[] files = new File(dirPath).listFiles();
        if (files != null) {
            return Stream.of(files).map(File::getAbsolutePath).collect(Collectors.toList());
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
}
