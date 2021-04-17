package wadosm.breweryhost;

public interface DriverFile {

    void write(byte[] b);

    void seek(long pos);

    byte[] read(int size);

    long length();
}
