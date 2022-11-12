package wadosm.breweryhost.logic.brewing.model;

import lombok.*;

@Value
@Builder
public class BrewingSnapshotState {
    @NonNull BrewingReadings readings;
    @NonNull BrewingSettings settings;
}
