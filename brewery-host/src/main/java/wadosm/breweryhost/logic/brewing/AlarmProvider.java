package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
class AlarmProvider {

    private final BreweryInterface breweryInterface;
    private final ConfigProvider configProvider;
    private final TimeProvider timeProvider;
    private final TemperatureThresholdTrigger trigger;
    private Instant alarmStarted;

    void handleAlarm(Double currentTemperature) {
        Configuration configuration = configProvider.loadConfiguration();
        boolean alarmEnabled = isAlarmEnabled(currentTemperature, configuration.getBrewingSettings());
        Duration alarmMaxTime = configuration.getAlarmMaxTime();

        setAlarmStartedTime(alarmEnabled, alarmMaxTime);

        breweryInterface.setAlarm(isAlarmEnabled(alarmEnabled, alarmMaxTime));
    }

    private boolean isAlarmEnabled(boolean alarmEnabled, Duration alarmMaxTime) {
        if (alarmEnabled) {
            if (alarmMaxTime != null) {
                if (alarmStarted != null) {
                    if (Duration.between(alarmStarted, timeProvider.getCurrentTime()).compareTo(alarmMaxTime) > 0) {
                        alarmEnabled = false;
                    }

                }
            }
        }
        return alarmEnabled;
    }

    private void setAlarmStartedTime(boolean alarmEnabled, Duration alarmMaxTime) {
        if (alarmEnabled) {
            if (alarmMaxTime != null) {
                if (alarmStarted == null) {
                    alarmStarted = timeProvider.getCurrentTime();
                }
            }
        } else {
            alarmStarted = null;
        }
    }

    private boolean isAlarmEnabled(Double currentTemperature, BrewingSettings brewingSettings) {
        return isEnabled(brewingSettings) && trigger.isTriggered(currentTemperature);
    }

    private static boolean isEnabled(BrewingSettings brewingSettings) {
        return brewingSettings.isEnabled() && brewingSettings.isTemperatureAlarmEnabled();
    }

}
