package wadosm.breweryhost.logic.brewing;

public interface TemperatureThresholdTrigger {
    boolean isTriggered(Double currentTemperature);
}
