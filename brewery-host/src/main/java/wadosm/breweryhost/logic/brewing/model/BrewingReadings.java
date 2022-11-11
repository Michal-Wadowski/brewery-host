package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class BrewingReadings {
    private final Integer heatingPower;
    private final List<TemperatureSensor> currentTemperature;
}
