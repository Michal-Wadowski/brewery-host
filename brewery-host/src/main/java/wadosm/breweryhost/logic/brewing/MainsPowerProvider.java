package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

@RequiredArgsConstructor
public
class MainsPowerProvider {

    private final BrewingSettingsProvider brewingSettingsProvider;
    private final BreweryInterface breweryInterface;

    void setMainsPower(Float currentTemperature) {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();

        if (brewingSettings.isEnabled() && currentTemperature != null
                && brewingSettings.getDestinationTemperature() != null
                && currentTemperature < brewingSettings.getDestinationTemperature()
        ) {
            int driveMaxPower = 0xff;
            if (brewingSettings.getMaxPower() != null) {
                driveMaxPower = (int) (brewingSettings.getMaxPower() / 100.0 * 0xff);
            }

            int drivePower = 0xff;
            if (brewingSettings.getPowerTemperatureCorrelation() != null) {
                drivePower = (int) (
                        (brewingSettings.getDestinationTemperature() - currentTemperature) * brewingSettings.getPowerTemperatureCorrelation()
                );
            }

            if (drivePower > driveMaxPower) {
                drivePower = driveMaxPower;
            } else if (drivePower < 0) {
                drivePower = 0;
            }

            breweryInterface.setMainsPower(1, drivePower);
            breweryInterface.setMainsPower(2, drivePower);
        } else {
            breweryInterface.setMainsPower(1, 0);
            breweryInterface.setMainsPower(2, 0);
        }
    }
}
