package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.util.List;

@Value
@Jacksonized
@Builder
public class BrewingSchedule {

    @Builder.Default
    @NonNull List<ScheduleStep> scheduleSteps = List.of();

    @Value
    private static class ScheduleStep {

        Duration startAfter;

        Duration duration;

        Double temperature;

        Boolean motor;

        Integer maxPower;

        Double powerTemperatureCorrelation;

        Boolean alarm;

    }
}
