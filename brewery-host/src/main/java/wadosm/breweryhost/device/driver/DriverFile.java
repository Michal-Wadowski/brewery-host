package wadosm.breweryhost.device.driver;

public interface DriverFile {

    void write(byte[] b);

    void seek(long pos);

    byte[] read(int size);

    Long length();

    void open();

    void close();

    boolean isOpened();
}
