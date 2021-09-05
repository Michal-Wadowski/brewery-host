package wadosm.breweryhost.device.driver;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Component
@Scope("prototype")
public class DriverFileImpl implements DriverFile {

    private RandomAccessFile file;

    private String getFilePath() {
        return "/dev/shm/driver.bin";
    }

    @Override
    public void write(byte[] b) {

        if (file == null) {
            return;
        }

        try {
            file.write(b);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void seek(long pos) {
        if (file == null) {
            return;
        }

        try {
            file.seek(pos);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public byte[] read(int size) {
        if (file == null) {
            return null;
        }

        try {
            byte[] buffer = new byte[size];
            file.read(buffer, 0, size);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            close();
            return null;
        }
    }

    @Override
    public Long length() {
        if (file == null) {
            return null;
        }
        try {
            return file.length();
        } catch (IOException e) {
            close();
            return null;
        }
    }

    @Override
    public void open() {
        try {
            if (file != null) {
                close();
            }

            file = new RandomAccessFile(getFilePath(), "rws");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (file != null) {
                file.close();
                file = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOpened() {
        return file != null;
    }
}
