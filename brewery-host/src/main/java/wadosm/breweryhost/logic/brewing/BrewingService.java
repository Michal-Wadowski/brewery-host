package wadosm.breweryhost.logic.brewing;

import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;

public interface BrewingService {

    void enable(boolean enable);

    void setDestinationTemperature(Double temperature);

    void enableTemperatureAlarm(boolean enable);

    void setMaxPower(Integer powerInPercents);

    void setPowerTemperatureCorrelation(Double percentagesPerDegree);

    void motorEnable(boolean enable);

    BrewingSnapshotState getBrewingSnapshotState();

    void processStep();

    void heartbeat();

    void calibrateTemperature(Integer side, Double value);
}
