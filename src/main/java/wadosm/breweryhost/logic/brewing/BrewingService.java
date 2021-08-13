package wadosm.breweryhost.logic.brewing;

public interface BrewingService {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    void enableTemperatureAlarm(boolean enable);

    void setMaxPower(Integer powerInPercents);

    void setPowerTemperatureCorrelation(Float percentagesPerDegree);

    void setTimer(int seconds);

    void removeTimer();

    void motorEnable(boolean enable);

    BrewingState getBrewingState();
}
