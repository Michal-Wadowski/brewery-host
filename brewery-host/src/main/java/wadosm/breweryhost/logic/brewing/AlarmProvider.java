package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
class AlarmProvider {

    private final BreweryInterface breweryInterface;
    private final ConfigProvider configProvider;
    private final TimeProvider timeProvider;
    private Instant alarmStarted;

    private Boolean alarmWasEnabledBefore = false;

    void handleAlarm(Double currentTemperature) {
        Configuration configuration = configProvider.loadConfiguration();

        boolean alarmTriggered = temperatureExceededThreshold(configuration, currentTemperature);

        setAlarmStartedTime(configuration, alarmTriggered);

        if (isAlarmBecameDisabled(configuration)) {
            breweryInterface.setAlarm(false);
        } else if (isTemperatureAlarmEnabled(configuration)) {
            breweryInterface.setAlarm(isAlarmPending(configuration, alarmTriggered));
        }
    }

    private boolean isAlarmBecameDisabled(Configuration configuration) {
        boolean temperatureAlarmEnabled = isTemperatureAlarmEnabled(configuration);
        boolean becameDisabled = alarmWasEnabledBefore && !temperatureAlarmEnabled;
        alarmWasEnabledBefore = temperatureAlarmEnabled;
        return becameDisabled;
    }

    private boolean isAlarmPending(Configuration configuration, boolean alarmTriggered) {
        if (alarmTriggered) {
            if (configuration != null) {
                if (alarmStarted != null) {
                    if (Duration.between(alarmStarted, timeProvider.getCurrentTime()).compareTo(configuration.getAlarmMaxTime()) > 0) {
                        alarmTriggered = false;
                    }

                }
            }
        }
        return alarmTriggered;
    }

    private void setAlarmStartedTime(Configuration configuration, boolean alarmTriggered) {
        if (alarmTriggered && isTemperatureAlarmEnabled(configuration)) {
            if (configuration.getAlarmMaxTime() != null) {
                if (alarmStarted == null) {
                    alarmStarted = timeProvider.getCurrentTime();
                }
            }
        } else {
            alarmStarted = null;
        }
    }

    private static boolean isTemperatureAlarmEnabled(Configuration configuration) {
        BrewingSettings brewingSettings = configuration.getBrewingSettings();
        return brewingSettings.isTemperatureAlarmEnabled() && brewingSettings.isEnabled();
    }

    private static boolean temperatureExceededThreshold(Configuration configuration, Double currentTemperature) {
        BrewingSettings brewingSettings = configuration.getBrewingSettings();
        return brewingSettings.getDestinationTemperature() != null & currentTemperature != null
                && currentTemperature >= brewingSettings.getDestinationTemperature();
    }

}
