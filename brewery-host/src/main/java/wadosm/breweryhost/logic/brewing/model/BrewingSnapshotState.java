package wadosm.breweryhost.logic.brewing.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class BrewingSnapshotState {
    private final BrewingReadings readings;
    private final BrewingSettings settings;
}
