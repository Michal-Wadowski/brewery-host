package wadosm.breweryhost.temperature;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.filesystem.FilesManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemperatureSensorsReaderImplTest {

    @Test
    public void readSensors_returns_empty_if_not_sensors_available() {
        // given
        FilesManager filesManager = new FakeFileManager();
        TemperatureSensorsReader sensorsReader = new TemperatureSensorsReaderImpl(filesManager);

        // when
        List<TemperatureSensor> sensors = sensorsReader.readSensors();

        // then
        assertThat(sensors).isNotNull().hasSize(0);
    }

    @Test
    public void read_all_temperature_sensors() {
        // given
        FakeFileManager filesManager = new FakeFileManager();
        filesManager.mockFile(
                "/sys/bus/w1/devices/w1_bus_master1/w1_master_slaves",
                ("28-0417003841ff\n" +
                        "28-0317002467ff\n" +
                        "tainted\n").getBytes()
        );

        filesManager.mockFile(
                "/sys/bus/w1/devices/28-0417003841ff/temperature",
                ("24125\n").getBytes()
        );

        filesManager.mockFile(
                "/sys/bus/w1/devices/28-0317002467ff/temperature",
                ("\n23125").getBytes()
        );

        filesManager.mockFile(
                "/sys/bus/w1/devices/tainted/temperature",
                ("poison").getBytes()
        );
        TemperatureSensorsReader sensorsReader = new TemperatureSensorsReaderImpl(filesManager);

        // when
        List<TemperatureSensor> sensors = sensorsReader.readSensors();

        // then
        assertThat(sensors).isNotNull().hasSize(2);
        assertThat(sensors.get(0).getSensorId()).isEqualTo("28-0417003841ff");
        assertThat(sensors.get(0).getTemperature()).isEqualTo(24125);

        assertThat(sensors.get(1).getSensorId()).isEqualTo("28-0317002467ff");
        assertThat(sensors.get(1).getTemperature()).isEqualTo(23125);
    }

    private static class FakeFileManager implements FilesManager {

        Map<String, byte[]> mockedFiles = new HashMap<>();

        @Override
        public boolean isFileAccessible(String filePath) {
            return true;
        }

        @Override
        public boolean moveFile(String source, String destination) {
            return false;
        }

        @Override
        public List<String> listFiles(String dirPath, String basename) {
            return null;
        }

        @Override
        public List<String> listFiles(String dirPath, String basename, String extension) {
            return null;
        }

        public void mockFile(String path, byte[] content) {
            mockedFiles.put(path, content);
        }

        @Override
        public byte[] readFile(String path) {
            return mockedFiles.get(path);
        }

        @Override
        public boolean isFileChecksumValid(String srcFile) {
            return false;
        }

        @Override
        public void deleteFile(String filePath) {

        }

        @Override
        public boolean copyFile(String source, String destination) {
            return false;
        }

        @Override
        public void deleteFiles(List<String> files) {

        }

    }
}