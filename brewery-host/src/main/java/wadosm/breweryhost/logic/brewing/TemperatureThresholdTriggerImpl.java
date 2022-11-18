package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;

@RequiredArgsConstructor
class TemperatureThresholdTriggerImpl implements TemperatureThresholdTrigger {

    private final ConfigProvider configProvider;

    @Override
    public boolean isTriggered(Double currentTemperature) {
        BrewingSettings brewingSettings = configProvider.loadConfiguration().getBrewingSettings();
        return brewingSettings.getDestinationTemperature() != null & currentTemperature != null
                && currentTemperature >= brewingSettings.getDestinationTemperature();
    }
}
