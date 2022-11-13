package wadosm.breweryhost.logic.brewing;

import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

public interface BrewingSettingsProvider {
    BrewingSettings getBrewingSettings();

    void setEnabled(boolean enable);

    void setDestinationTemperature(Float temperature);

    void setTemperatureAlarmEnabled(boolean enable);

    void setMaxPower(Integer powerInPercents);

    void setMotorEnabled(boolean enable);

    void setPowerTemperatureCorrelation(Float percentagesPerDegree);
}
