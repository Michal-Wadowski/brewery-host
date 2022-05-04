package wadosm.breweryhost;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
javac -h . driver/src/main/java/wadosm/breweryhost/DigiPort.java driver/src/main/java/wadosm/breweryhost/DriverEntry.java
 */
@Log4j2
public class DriverEntryImpl implements DriverEntry {

    static {
        try {
            loadNativeLibrary("/brewery_driver.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNativeLibrary(String name) throws IOException {
        log.info("Library " + name + " loading...");

        InputStream in = DriverEntryImpl.class.getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        FileOutputStream fos = new FileOutputStream(temp);

        while((read = in.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.close();
        in.close();

        System.load(temp.getAbsolutePath());

        new File(temp.getAbsolutePath()).delete();
    }
}
