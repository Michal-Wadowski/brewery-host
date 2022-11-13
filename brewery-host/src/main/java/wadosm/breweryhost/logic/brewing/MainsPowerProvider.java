package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

@Component
@RequiredArgsConstructor
public
class MainsPowerProvider {

    private final BrewingSettingsProvider brewingSettingsProvider;
    private final BreweryInterface breweryInterface;

    private Float currentPower = 0f;

    void updatePowerForTemperature(Float currentTemperature) {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();

        if (brewingSettings.isEnabled() && currentTemperature != null
                && brewingSettings.getDestinationTemperature() != null
                && currentTemperature < brewingSettings.getDestinationTemperature()
        ) {
            float driveMaxPower = 1.0f;
            if (brewingSettings.getMaxPower() != null) {
                driveMaxPower = brewingSettings.getMaxPower() / 100.0f;
            }

            currentPower = 1.0f;
            if (brewingSettings.getPowerTemperatureCorrelation() != null) {
                currentPower = (
                        (brewingSettings.getDestinationTemperature() - currentTemperature) * brewingSettings.getPowerTemperatureCorrelation() / 100.0f
                );
            }

            if (currentPower > driveMaxPower) {
                currentPower = driveMaxPower;
            } else if (currentPower < 0) {
                currentPower = 0f;
            }
        } else {
            currentPower = 0f;
        }

        breweryInterface.setMainsPower(1, (int) (currentPower * 0xff));
        breweryInterface.setMainsPower(2, (int) (currentPower * 0xff));
    }

    public Integer getCurrentPower() {
        return (int) (currentPower * 100);
    }
}
