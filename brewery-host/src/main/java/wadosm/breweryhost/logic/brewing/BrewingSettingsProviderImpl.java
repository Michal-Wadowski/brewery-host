package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class BrewingSettingsProviderImpl implements BrewingSettingsProvider {

    private final ConfigProvider configProvider;

    @Override
    public BrewingSettings getBrewingSettings() {
        return configProvider.loadConfiguration().getBrewingSettings();
    }

    @Override
    public void setEnabled(boolean enable) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withEnabled(enable));
    }

    @Override
    public void setDestinationTemperature(Double temperature) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withDestinationTemperature(temperature));
    }

    @Override
    public void setTemperatureAlarmEnabled(boolean enable) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withTemperatureAlarmEnabled(enable));
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withMaxPower(powerInPercents));
    }

    @Override
    public void setMotorEnabled(boolean enable) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withMotorEnabled(enable));
    }

    @Override
    public void setPowerTemperatureCorrelation(Double percentagesPerDegree) {
        updateAndSaveSettings(brewingSettings -> brewingSettings.withPowerTemperatureCorrelation(percentagesPerDegree));
    }

    private void updateAndSaveSettings(Function<BrewingSettings, BrewingSettings> updateBrewingSettings) {
        configProvider.updateAndSaveConfiguration(
                configuration -> configuration.withBrewingSettings(updateBrewingSettings.apply(configuration.getBrewingSettings()))
        );
    }
}
