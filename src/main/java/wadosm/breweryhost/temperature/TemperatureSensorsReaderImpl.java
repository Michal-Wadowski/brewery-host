package wadosm.breweryhost.temperature;

import org.springframework.stereotype.Component;
import wadosm.breweryhost.filesystem.FilesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TemperatureSensorsReaderImpl implements TemperatureSensorsReader {

    FilesManager filesManager;

    public TemperatureSensorsReaderImpl(FilesManager filesManager) {
        this.filesManager = filesManager;
    }

    @Override
    public List<TemperatureSensor> readSensors() {
        byte[] slavesListRaw = filesManager.readFile(
                "/sys/bus/w1/devices/w1_bus_master1/w1_master_slaves"
        );
        if (slavesListRaw != null) {
            String slavesList = new String(slavesListRaw);

            List<TemperatureSensor> result = slavesList.lines().map(sensorId -> {
                if (sensorId == null || sensorId.trim().length() == 0) {
                    return null;
                }

                byte[] sensorData = filesManager.readFile(
                        String.format("/sys/bus/w1/devices/%s/temperature", sensorId)
                );

                if (sensorData == null) {
                    return null;
                }

                try {
                    Integer temperature = Integer.valueOf(new String(sensorData).trim());
                    return new TemperatureSensor(sensorId, temperature);
                } catch (NumberFormatException e) {
                    return null;
                }

            }).filter(Objects::nonNull).collect(Collectors.toList());

            return result;
        }

        return new ArrayList<>();
    }
}
