package wadosm.breweryhost;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/*
javac -h . driver/src/main/java/wadosm/breweryhost/DigiPort.java \
driver/src/main/java/wadosm/breweryhost/DigiPortImpl.java \
driver/src/main/java/wadosm/breweryhost/DriverLoader.java
 */
@Log4j2
@RequiredArgsConstructor
public class DriverLoaderImpl implements DriverLoader {

    private final Environment environment;

    @Override
    @PostConstruct
    public void init() {
        try {
            boolean isLocalOrTestProfile = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(profiles -> profiles.contains("local") || profiles.contains("test"));
            if (isLocalOrTestProfile) {
                loadNativeLibrary("/brewery_driver_demo.so");
            } else {
                loadNativeLibrary("/brewery_driver.so");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadNativeLibrary(String name) throws IOException {
        log.info("Library " + name + " loading...");

        InputStream in = DriverLoaderImpl.class.getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        FileOutputStream fos = new FileOutputStream(temp);

        while ((read = in.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }

        fos.close();
        in.close();

        System.load(temp.getAbsolutePath());

        new File(temp.getAbsolutePath()).delete();
    }
}
