package wadosm.breweryhost.controller.brewing;

import wadosm.breweryhost.controller.fermenter.FermentingStatus;

public interface BrewingController {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    void setMaxPower(Integer powerInPercents);

    void setPowerTemperatureCorrelation(Float percentagesPerDegree);

    void setTimer(int seconds);

    void removeTimer();

    void motorEnable(boolean enable);

    FermentingStatus getBrewingStatus();
}
