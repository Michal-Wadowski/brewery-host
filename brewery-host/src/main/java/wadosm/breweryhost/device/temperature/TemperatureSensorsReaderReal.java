package wadosm.breweryhost.device.temperature;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.filesystem.FilesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Profile("!demo")
public class TemperatureSensorsReaderReal implements TemperatureSensorsReader {

    FilesManager filesManager;

    public TemperatureSensorsReaderReal(FilesManager filesManager) {
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
