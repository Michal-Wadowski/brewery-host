package wadosm.breweryhost.controller.fermenter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FermentingStatus {

    private final boolean enabled;

    private final Float currentTemperature;

    private final Float destinationTemperature;

    private final boolean heating;

}
