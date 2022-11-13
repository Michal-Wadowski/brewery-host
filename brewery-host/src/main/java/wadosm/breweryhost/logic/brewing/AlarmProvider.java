package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
class AlarmProvider {

    private final BreweryInterface breweryInterface;
    private final BrewingSettingsProvider brewingSettingsProvider;
    private final ConfigProvider configProvider;
    private final TimeProvider timeProvider;
    private Instant alarmStarted;

    void handleAlarm(Float currentTemperature) {
        boolean alarmEnabled = isAlarmEnabled(currentTemperature);
        Duration alarmMaxTime = configProvider.loadConfiguration().getAlarmMaxTime();

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

    private boolean isAlarmEnabled(Float currentTemperature) {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();
        return isEnabled(brewingSettings) && temperatureExceededThreshold(brewingSettings, currentTemperature);
    }

    private static boolean temperatureExceededThreshold(BrewingSettings brewingSettings, Float currentTemperature) {
        return brewingSettings.getDestinationTemperature() != null & currentTemperature != null
                && currentTemperature >= brewingSettings.getDestinationTemperature();
    }

    private static boolean isEnabled(BrewingSettings brewingSettings) {
        return brewingSettings.isEnabled() && brewingSettings.isTemperatureAlarmEnabled();
    }

}
