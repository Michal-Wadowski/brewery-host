package wadosm.breweryhost.device.filesystem;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilesManagerImplTest {

    @Test
    void readFile_success() {
        // given
        FilesManager filesManager = new FilesManagerImpl();

        // when
        byte[] content = filesManager.readFile("pom.xml");

        // then
        assertThat(content).isNotEmpty();
        assertThat(new String(content)).startsWith("<?xml");
    }

    @Test
    void readFile_unreadable() {
        // given
        FilesManager filesManager = new FilesManagerImpl();

        // when
        byte[] content = filesManager.readFile("/nonexistent-file");

        // then
        assertThat(content).isNull();
    }

    @Test
    void isFileAccessible_success() {
        // given
        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileAccessible = filesManager.isFileAccessible("pom.xml");

        // then
        assertThat(fileAccessible).isTrue();
    }

    @Test
    void isFileAccessible_not_accessible() {
        // given
        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileAccessible = filesManager.isFileAccessible("/bin/sh");

        // then
        assertThat(fileAccessible).isFalse();
    }

    @Test
    void moveFile_success() {
        // given
        try {
            new File("/tmp/from").createNewFile();
        } catch (IOException e) {
        }
        new File("/tmp/to").delete();
        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileMoved = filesManager.moveFile("/tmp/from", "/tmp/to");

        // then
        assertThat(fileMoved).isTrue();
        assertThat(new File("/tmp/from")).doesNotExist();
        assertThat(new File("/tmp/to")).exists();
    }

    @Test
    void moveFile_failed() {
        // given
        try {
            new File("/tmp/from").createNewFile();
        } catch (IOException e) {
        }
        new File("/root/to").delete();
        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileMoved = filesManager.moveFile("/tmp/from", "/root/to");

        // then
        assertThat(fileMoved).isFalse();
        assertThat(new File("/tmp/from")).exists();
        assertThat(new File("/root/to")).doesNotExist();
    }

    @Test
    void copyFile_success() {
        // given
        createHelloFile("/tmp/from");

        new File("/tmp/to").delete();
        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileMoved = filesManager.copyFile("/tmp/from", "/tmp/to");

        // then
        assertThat(fileMoved).isTrue();
        assertThat(new File("/tmp/from")).exists().hasContent("hello");
        assertThat(new File("/tmp/to")).exists().hasContent("hello");
    }

    @Test
    void copyFile_failed() {
        // given
        createHelloFile("/tmp/from");

        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean fileMoved = filesManager.copyFile("/tmp/from", "/root/to");

        // then
        assertThat(fileMoved).isFalse();
        assertThat(new File("/tmp/from")).exists().hasContent("hello");
        assertThat(new File("/root/to")).doesNotExist();
    }

    @Test
    void listFiles_with_mask() {
        // given
        try {
            new File("/tmp/listFiles").createNewFile();
            new File("/tmp/listFiles-part").createNewFile();
            new File("/tmp/listFiles-part.jar").createNewFile();
        } catch (IOException e) {
        }

        FilesManager filesManager = new FilesManagerImpl();

        // when
        List<String> paths = filesManager.listFiles("/tmp", "listFiles");

        // then
        assertThat(paths).hasSize(3);
    }

    @Test
    void listFiles_with_mask_and_extension() {
        // given
        try {
            new File("/tmp/listFiles").createNewFile();
            new File("/tmp/listFiles-part").createNewFile();
            new File("/tmp/listFiles-part.jar").createNewFile();
        } catch (IOException e) {
        }

        FilesManager filesManager = new FilesManagerImpl();

        // when
        List<String> paths = filesManager.listFiles("/tmp", "listFiles");

        // then
        assertThat(paths).hasSize(1).contains("/tmp/listFiles-part.jar");
    }

    @Test
    void deleteFile() {
        // given
        try {
            new File("/tmp/file").createNewFile();
        } catch (IOException e) {
        }

        FilesManager filesManager = new FilesManagerImpl();

        // when
        filesManager.deleteFile("/tmp/file");

        // then
        assertThat(new File("/tmp/file")).doesNotExist();
    }

    @Test
    void isFileChecksumValid_failed_because_name() {
        // given
        createHelloFile("/tmp/file");

        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean result = filesManager.isFileChecksumValid("/tmp/file");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isFileChecksumValid_failed_because_crc32_not_match() {
        // given
        createHelloFile("/tmp/file-12345678");

        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean result = filesManager.isFileChecksumValid("/tmp/file-12345678");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isFileChecksumValid_success() {
        // given
        createHelloFile("/tmp/file-3610a686");

        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean result = filesManager.isFileChecksumValid("/tmp/file-3610a686");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isFileChecksumValid_success_jar() {
        // given
        createHelloFile("/tmp/file-3610a686.jar");

        FilesManager filesManager = new FilesManagerImpl();

        // when
        boolean result = filesManager.isFileChecksumValid("/tmp/file-3610a686.jar");

        // then
        assertThat(result).isTrue();
    }

    private void createHelloFile(String pathname) {
        try {
            File file = new File(pathname);
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("hello");
            fileWriter.close();
        } catch (IOException e) {
        }
    }
}