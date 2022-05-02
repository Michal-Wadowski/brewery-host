package wadosm.breweryhost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DriverEntry {

    static {
        try {
            loadNativeLibrary("/brewery_driver.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public native void sayHello();

    public static void loadNativeLibrary(String name) throws IOException {
        InputStream in = DriverEntry.class.getResourceAsStream(name);
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
    }
}
