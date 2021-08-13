package wadosm.breweryhost.logic.fermenting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FermentingState {

    private final boolean enabled;

    private final Float currentTemperature;

    private final Float destinationTemperature;

    private final boolean heating;

}
