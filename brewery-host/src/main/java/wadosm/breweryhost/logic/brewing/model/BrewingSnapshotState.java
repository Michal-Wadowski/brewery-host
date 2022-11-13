package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class BrewingSnapshotState {
    @NonNull BrewingReadings readings;
    @NonNull BrewingSettings settings;
}
