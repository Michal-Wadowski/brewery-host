package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BrewingServiceImpl implements BrewingService {

    private final BreweryInterface breweryInterface;
    private final ConfigProvider configProvider;
    private final BrewingSettingsProvider brewingSettingsProvider;
    private final TemperatureProvider temperatureProvider;
    private final MainsPowerProvider mainsPowerProvider;
    private final AlarmProvider alarmProvider;
    private boolean heartBeatState;

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
    public void enableTemperatureAlarm(boolean enable) {
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
    public void processStep() {
        Configuration configuration = configProvider.loadConfiguration();

        Double selectedAvgTemperature = temperatureProvider.getSelectedAverageTemperatures();

        mainsPowerProvider.updatePowerForTemperature(selectedAvgTemperature);

        driveMotor(configuration);

        alarmProvider.handleAlarm(selectedAvgTemperature);

        displayTemperature(selectedAvgTemperature);
    }

    @Override
    @Async
    @Scheduled(fixedRateString = "${brewing.heartbeat}")
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
