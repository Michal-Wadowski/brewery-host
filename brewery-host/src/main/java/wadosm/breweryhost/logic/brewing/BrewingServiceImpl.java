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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Double usedTemperature = temperatureProvider.getUsedTemperature();

        mainsPowerProvider.updatePowerForTemperature(usedTemperature);

        driveMotor(configuration);

        alarmProvider.handleAlarm(usedTemperature);

        displayTemperature(usedTemperature);
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
            if (currentTemperature < 100) {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 100), 1 << 6, false, 4, 0);
            } else {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 10), 1 << 5, false, 4, 0);
            }
        } else {
            breweryInterface.displayClear(0);
        }
    }

    public void calibrateTemperature(Integer side, Double value) {
//        Configuration configuration = configProvider.loadConfiguration();
//
//        configuration = updateTemperatureCalibrationMeasurements(configuration, side, value);
//
//        configuration = updateTemperatureCalibration(configuration);
//
//        configProvider.saveConfiguration(configuration);
    }

    private Configuration updateTemperatureCalibration(Configuration configuration) {
        return configuration;
        // TODO
//        List<Double> measurements =
//                configuration.getTemperatureCalibrationMeasurements().get(getUsedSensorId(configuration));
//
//        Configuration.ConfigurationBuilder configurationBuilder = configuration.toBuilder();
//
//        if (measurements.stream().filter(Objects::nonNull).count() != 4) {
//            configurationBuilder.temperatureCalibration(Map.of());
//        } else {
//            Map<String, List<Double>> currCalibration = configuration.getTemperatureCalibration();
//            if (currCalibration == null) {
//                currCalibration = Map.of();
//            }
//            currCalibration = new HashMap<>(currCalibration);
//
//            var x1 = measurements.get(0);
//            var x2 = measurements.get(2);
//            var t1 = measurements.get(1);
//            var t2 = measurements.get(3);
//            var a = (t2 - t1) / (x2 - x1);
//            var b = -a * x1 + t1;
//
//            List<Double> currCalibrations = Arrays.asList(a, b);
//            // TODO
//            currCalibration.put(getUsedSensorId(configuration), currCalibrations);
//            configurationBuilder.temperatureCalibration(currCalibration);
//        }
//
//        return configurationBuilder.build();
    }

    private Configuration updateTemperatureCalibrationMeasurements(Configuration configuration, Integer side,
                                                                   Double value) {
        Map<String, List<Double>> allMeasurements;
        if (configuration.getTemperatureCalibrationMeasurements() == null) {
            allMeasurements = new HashMap<>();
        } else {
            allMeasurements = new HashMap<>(configuration.getTemperatureCalibrationMeasurements());
        }

        // TODO:
//        String brewingSensorId = getUsedSensorId(configuration);
//        List<Double> currMeasurements;
//        if (!allMeasurements.containsKey(brewingSensorId) ||
//                allMeasurements.get(brewingSensorId).size() != 4
//        ) {
//            currMeasurements = Arrays.asList(null, null, null, null);
//        } else {
//            currMeasurements = allMeasurements.get(brewingSensorId);
//        }
//
//        String usedSensorId = getUsedSensorId(configuration);
//        if (side == 0) {
//            currMeasurements.set(0, getUncalibratedTemperature(usedSensorId));
//            currMeasurements.set(1, value);
//        } else if (side == 1) {
//            currMeasurements.set(2, getUncalibratedTemperature(usedSensorId));
//            currMeasurements.set(3, value);
//        }

//        allMeasurements.put(brewingSensorId, currMeasurements);

        return configuration.withTemperatureCalibrationMeasurements(allMeasurements);
    }


    private void driveMotor(Configuration configuration) {
        BrewingSettings brewingSettings = brewingSettingsProvider.getBrewingSettings();
        breweryInterface.motorEnable(
                configuration.getBrewingMotorNumber(),
                brewingSettings.isEnabled() && brewingSettings.isMotorEnabled()
        );
    }

}
