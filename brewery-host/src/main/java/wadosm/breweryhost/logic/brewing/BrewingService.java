package wadosm.breweryhost.logic.brewing;

public interface BrewingService {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    void enableTemperatureAlarm(boolean enable);

    void setMaxPower(Integer powerInPercents);

    void setPowerTemperatureCorrelation(Float percentagesPerDegree);

    void motorEnable(boolean enable);

    BrewingState getBrewingState();

    void processStep();

    void heartbeat();

    void calibrateTemperature(Integer side, Float value);
}
