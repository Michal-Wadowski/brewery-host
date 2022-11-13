package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Value
public class BrewingReadings {
    @NotNull Integer heatingPower;
    @NonNull List<TemperatureSensor> currentTemperature;
}
