package wadosm.breweryhost.device.temperature;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Profile("!local")
public class TemperatureSensorsReaderReal implements TemperatureSensorsReader {

    @Override
    public List<TemperatureSensor> readSensors() {
        byte[] slavesListRaw = readFile(
                "/sys/bus/w1/devices/w1_bus_master1/w1_master_slaves"
        );
        if (slavesListRaw != null) {
            String slavesList = new String(slavesListRaw);

            List<TemperatureSensor> result = slavesList.lines().map(sensorId -> {
                if (sensorId == null || sensorId.trim().length() == 0) {
                    return null;
                }

                byte[] sensorData = readFile(
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

    private byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
    }
}
