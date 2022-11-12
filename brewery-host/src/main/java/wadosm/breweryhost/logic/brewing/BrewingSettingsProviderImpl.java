package wadosm.breweryhost.logic.brewing;

import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

@Component
public class BrewingSettingsProviderImpl implements BrewingSettingsProvider {

    private BrewingSettings brewingSettings = BrewingSettings.builder().build();

    @Override
    public BrewingSettings getBrewingSettings() {
        return brewingSettings;
    }

    @Override
    public void setEnabled(boolean enable) {
        brewingSettings = brewingSettings.withEnabled(enable);
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        brewingSettings = brewingSettings.withDestinationTemperature(temperature);
    }

    @Override
    public void setTemperatureAlarmEnabled(boolean enable) {
        brewingSettings = brewingSettings.withTemperatureAlarmEnabled(enable);
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        brewingSettings = brewingSettings.withMaxPower(powerInPercents);
    }

    @Override
    public void setMotorEnabled(boolean enable) {
        brewingSettings = brewingSettings.withMotorEnabled(enable);
    }

    @Override
    public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
        brewingSettings = brewingSettings.withPowerTemperatureCorrelation(percentagesPerDegree);
    }
}
