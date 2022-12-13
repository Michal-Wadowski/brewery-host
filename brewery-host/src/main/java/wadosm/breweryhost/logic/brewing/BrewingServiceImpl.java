package wadosm.breweryhost.logic.brewing;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingReadings;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

@Service
@Log4j2
@EnableAsync
public class BrewingServiceImpl implements BrewingService {

    private final BreweryInterface breweryInterface;
    private final ConfigProvider configProvider;
    private final BrewingSettingsProvider brewingSettingsProvider;
    private final TemperatureProvider temperatureProvider;
    private final MainsPowerProvider mainsPowerProvider;
    private final TimeProvider timeProvider;
    private AlarmProvider alarmProvider;
    private boolean heartBeatState;

    public BrewingServiceImpl(BreweryInterface breweryInterface,
                              ConfigProvider configProvider,
                              BrewingSettingsProvider brewingSettingsProvider,
                              TemperatureProvider temperatureProvider,
                              MainsPowerProvider mainsPowerProvider,
                              TimeProvider timeProvider
    ) {
        this.breweryInterface = breweryInterface;
        this.configProvider = configProvider;
        this.brewingSettingsProvider = brewingSettingsProvider;
        this.temperatureProvider = temperatureProvider;
        this.mainsPowerProvider = mainsPowerProvider;
        this.timeProvider = timeProvider;

        setAlarmMode(AlarmMode.THRESHOLD_TRIGGERED);
    }

    @Override
    public void setAlarmMode(AlarmMode alarmMode) {
        switch (alarmMode) {
            case THRESHOLD_TRIGGERED:
                alarmProvider = getThresholdTriggeredAlarmProvider(breweryInterface, configProvider, timeProvider);
                break;

            case MANUAL:
                alarmProvider = getAlwaysRunningAlarmProvider(breweryInterface, configProvider, timeProvider);
                break;
        }
    }

    private static AlarmProvider getThresholdTriggeredAlarmProvider(BreweryInterface breweryInterface, ConfigProvider configProvider, TimeProvider timeProvider) {
        return new AlarmProvider(breweryInterface, configProvider, timeProvider, new TemperatureThresholdTriggerImpl(configProvider));
    }

    private static AlarmProvider getAlwaysRunningAlarmProvider(BreweryInterface breweryInterface, ConfigProvider configProvider, TimeProvider timeProvider) {
        return new AlarmProvider(breweryInterface, configProvider, timeProvider, new TemperatureThresholdTriggerImpl(configProvider));
    }

    @Override
    public void enable(boolean enable) {
        brewingSettingsProvider.setEnabled(enable);
        processStep();
    }

    @Override
    public void setDestinationTemperature(Double temperature) {
        if (temperature == null || temperature >= 0 && temperature <= 100) {
            brewingSettingsProvider.setDestinationTemperature(temperature);
            processStep();
        }
    }

    @Override
    public void enableAlarm(boolean enable) {
        brewingSettingsProvider.setTemperatureAlarmEnabled(enable);
        processStep();
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        brewingSettingsProvider.setMaxPower(powerInPercents);
        processStep();
    }

    @Override
    public void motorEnable(boolean enable) {
        brewingSettingsProvider.setMotorEnabled(enable);
        processStep();
    }

    @Override
    public BrewingSnapshotState getBrewingSnapshotState() {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();

        return BrewingSnapshotState.builder()
                .readings(BrewingReadings.builder()
                        .heatingPower(getHeatingPower())
                        .currentTemperature(temperatureProvider.getAllTemperatures())
                        .build())

                .settings(BrewingSettings.builder()
                        .enabled(brewingSettings.isEnabled())
                        .destinationTemperature(brewingSettings.getDestinationTemperature())
                        .maxPower(brewingSettings.getMaxPower())
                        .powerTemperatureCorrelation(brewingSettings.getPowerTemperatureCorrelation())
                        .motorEnabled(brewingSettings.isMotorEnabled())
                        .temperatureAlarmEnabled(brewingSettings.isTemperatureAlarmEnabled())
                        .build())

                .build();
    }

    private Integer getHeatingPower() {
        return mainsPowerProvider.getCurrentPower();
    }

    @Override
    public void setPowerTemperatureCorrelation(Double percentagesPerDegree) {
        brewingSettingsProvider.setPowerTemperatureCorrelation(percentagesPerDegree);
        processStep();
    }


    @Async
    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    @Override
    public void processStep() {
        Configuration configuration = configProvider.loadConfiguration();

        Double selectedAvgTemperature = temperatureProvider.getSelectedAverageTemperatures();

        mainsPowerProvider.updatePowerForTemperature(selectedAvgTemperature);

        driveMotor(configuration);

        alarmProvider.handleAlarm(selectedAvgTemperature);

        displayTemperature(selectedAvgTemperature);
    }

    @Async
    @Scheduled(fixedRateString = "${brewing.heartbeat}")
    @Override
    public void heartbeat() {
        breweryInterface.heartbeat(heartBeatState);
        heartBeatState = !heartBeatState;
    }

    private void displayTemperature(Double currentTemperature) {
        if (brewingSettingsProvider.getBrewingSettings().isEnabled() && currentTemperature != null) {
            log.debug("### currentTemperature: {}", currentTemperature);
            if (currentTemperature < 100) {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 100), 1 << 6, false, 4, 0);
            } else {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 10), 1 << 5, false, 4, 0);
            }
        } else {
            breweryInterface.displayClear(0);
        }
    }

    private void driveMotor(Configuration configuration) {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();
        breweryInterface.motorEnable(
                configuration.getBrewingMotorNumber(),
                brewingSettings.isEnabled() && brewingSettings.isMotorEnabled()
        );
    }

}
