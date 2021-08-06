package wadosm.breweryhost.filesystem;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.filesystem.FilesManager;
import wadosm.breweryhost.filesystem.FilesManagerImpl;

import java.io.File;
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
    void listFiles() {
        // given
        FilesManager filesManager = new FilesManagerImpl();

        // when
        List<String> paths = filesManager.listFiles("/etc");

        // then
        assertThat(paths).isNotEmpty().contains("/etc/fstab");
    }
}