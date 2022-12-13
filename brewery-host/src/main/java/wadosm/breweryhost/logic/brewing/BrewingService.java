package wadosm.breweryhost.logic.brewing;

import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;

public interface BrewingService {

    void setAlarmMode(AlarmMode alarmMode);

    void enable(boolean enable);

    void setDestinationTemperature(Double temperature);

    void enableAlarm(boolean enable);

    void setMaxPower(Integer powerInPercents);

    void setPowerTemperatureCorrelation(Double percentagesPerDegree);

    void motorEnable(boolean enable);

    BrewingSnapshotState getBrewingSnapshotState();

    void processStep();

    void heartbeat();

}
