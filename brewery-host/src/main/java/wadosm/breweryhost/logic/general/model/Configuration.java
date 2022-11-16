package wadosm.breweryhost.logic.general.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.brewing.model.SensorsConfiguration;

import java.time.Duration;

@Jacksonized
@Builder
@Value
public class Configuration {

    @Builder.Default
    @With
    @NonNull Integer brewingMotorNumber = 1;

    @Builder.Default
    @With
    Duration alarmMaxTime = null;

    @Builder.Default
    @With
    @NonNull SensorsConfiguration sensorsConfiguration = SensorsConfiguration.builder().build();

    @Builder.Default
    @With
    @NonNull BrewingSettings brewingSettings = BrewingSettings.builder().build();

    @Builder.Default
    @With
    @NonNull BrewingSchedule brewingSchedule = BrewingSchedule.builder().build();

}
