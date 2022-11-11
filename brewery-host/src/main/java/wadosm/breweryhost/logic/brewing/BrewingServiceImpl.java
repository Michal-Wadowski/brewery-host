package wadosm.breweryhost.logic.brewing;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.device.driver.model.BreweryState;
import wadosm.breweryhost.device.temperature.TemperatureSensorProvider;
import wadosm.breweryhost.device.temperature.model.TemperatureSensor;
import wadosm.breweryhost.logic.brewing.model.BrewingReadings;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.brewing.model.BrewingSnapshotState;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Log4j2
@EnableAsync
public class BrewingServiceImpl implements BrewingService {

    private final BreweryInterface breweryInterface;

    private final TemperatureSensorProvider temperatureSensorProvider;
    private final ConfigProvider configProvider;

    private final BrewingSettings brewingSettings = new BrewingSettings();

    private boolean heartBeatState;

    public BrewingServiceImpl(
            BreweryInterface breweryInterface,
            TemperatureSensorProvider temperatureSensorProvider,
            ConfigProvider configProvider) {
        this.breweryInterface = breweryInterface;
        this.temperatureSensorProvider = temperatureSensorProvider;
        this.configProvider = configProvider;
    }

    @Override
    public void enable(boolean enable) {
        brewingSettings.setEnabled(enable);
        processStep();
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        if (temperature == null || temperature >= 0 && temperature <= 100) {
            brewingSettings.setDestinationTemperature(temperature);
            processStep();
        }
    }

    @Override
    public void enableTemperatureAlarm(boolean enable) {
        brewingSettings.setTemperatureAlarmEnabled(enable);
        processStep();
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        brewingSettings.setMaxPower(powerInPercents);
        processStep();
    }

    @Override
    public void motorEnable(boolean enable) {
        brewingSettings.setMotorEnabled(enable);
        processStep();
    }

    @Override
    public BrewingSnapshotState getBrewingSnapshotState() {
        Configuration configuration = configProvider.loadConfiguration();

        return BrewingSnapshotState.builder()
                .readings(BrewingReadings.builder()
                        .heatingPower(getHeatingPower())
                        .currentTemperature(getCurrentTemperature(configuration))
                        .build())

                .settings(BrewingSettings.builder()
                        .enabled(brewingSettings.isEnabled())
                        .destinationTemperature(brewingSettings.getDestinationTemperature())
                        .maxPower(brewingSettings.getMaxPower())
                        .powerTemperatureCorrelation(getPowerTemperatureCorrelation())
                        .motorEnabled(brewingSettings.isMotorEnabled())
                        .temperatureAlarmEnabled(brewingSettings.isTemperatureAlarmEnabled())
                        .build())

                .build();
    }

    private Integer getHeatingPower() {
        BreweryState breweryState = breweryInterface.readDriverInterfaceState();
        if (breweryState != null) {
            return (int) (breweryState.getMains(1) * 100.0 / 0xff);
        }
        return null;
    }

    // TODO: Move 0xff * 100 scalar to setMainsPower(). Write tests to handle this
    private Float getPowerTemperatureCorrelation() {
        if (brewingSettings.getPowerTemperatureCorrelation() != null) {
            return brewingSettings.getPowerTemperatureCorrelation() / 0xff * 100;
        } else {
            return null;
        }
    }

    @Override
    public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
        if (percentagesPerDegree != null) {
            this.brewingSettings.setPowerTemperatureCorrelation((float) (percentagesPerDegree / 100.0 * 0xff));
        } else {
            this.brewingSettings.setPowerTemperatureCorrelation(null);
        }
        processStep();
    }

    private Float getUncalibratedTemperature(String sensorId) {
        // TODO: Update here after tests about use multiple sensors
        TemperatureSensor temperatureSensor = TemperatureSensor.fromRaw(
                temperatureSensorProvider.getRawTemperatureSensor(sensorId)
        );

        if (temperatureSensor != null) {
            return temperatureSensor.getTemperature();
        } else {
            return null;
        }
    }

    private List<TemperatureSensor> getCurrentTemperature(Configuration configuration) {
        List<TemperatureSensor> result =
                configuration.getSensorsConfiguration().getShowBrewingSensorIds().stream().map(sensorId -> {
                            Float temperature = getCalibratedTemperature(configuration, sensorId);
                            if (temperature == null) {
                                return null;
                            }
                            return TemperatureSensor.builder()
                                    .sensorId(sensorId)
                                    .temperature(temperature)
                                    .build();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        boolean usedSameAsShown = configuration.getSensorsConfiguration().getUseBrewingSensorIds()
                .equals(configuration.getSensorsConfiguration().getShowBrewingSensorIds());
        boolean shownSingleSensor = configuration.getSensorsConfiguration().getShowBrewingSensorIds().size() == 1;

        if (!usedSameAsShown || !shownSingleSensor) {
            TemperatureSensor usedTemperature = getUsedTemperature(configuration);
            if (usedTemperature != null) {
                result.add(usedTemperature);
            }
        }

        return result;
    }

    private TemperatureSensor getUsedTemperature(Configuration configuration) {
        double usedTemperatures = configuration.getSensorsConfiguration().getUseBrewingSensorIds().stream()
                .map(sensorId -> getCalibratedTemperature(configuration, sensorId))
                .filter(Objects::nonNull)
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(usedTemperatures)) {
            return null;
        }
        return TemperatureSensor.builder()
                .sensorId("#use")
                .temperature((float) usedTemperatures)
                .build();
    }

    private Float getCalibratedTemperature(Configuration configuration, String shownSensorId) {
        Float temperature = getUncalibratedTemperature(shownSensorId);
        Float calibratedTemperature = null;

        if (temperature != null) {
            Map<String, List<Float>> temperatureCalibration = configuration.getTemperatureCalibration();

            // TODO: #2
            if (temperatureCalibration != null && temperatureCalibration.containsKey(shownSensorId)) {
                List<Float> sensorCalibration = temperatureCalibration.get(shownSensorId);
                if (sensorCalibration.size() == 2) {
                    temperature *= (1 + sensorCalibration.get(0));
                    temperature += sensorCalibration.get(1);
                }
            }

            calibratedTemperature = Math.round(temperature * 100) / 100.0f;
        }
        return calibratedTemperature;
    }

    @Async
    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    public void processStep() {
        Configuration configuration = configProvider.loadConfiguration();

        TemperatureSensor usedTemperature = getUsedTemperature(configuration);
        Float currentTemperature = usedTemperature != null ? usedTemperature.getTemperature() : null;

        setMainsPower(currentTemperature);

        driveMotor(configuration);

        breweryInterface.setAlarm(isAlarmEnabled(currentTemperature));

        displayTemperature(currentTemperature);
    }

    @Override
    @Async
    @Scheduled(fixedRateString = "${brewing.heartbeat}")
    public void heartbeat() {
        breweryInterface.heartbeat(heartBeatState);
        heartBeatState = !heartBeatState;
    }

    private void displayTemperature(Float currentTemperature) {
        if (brewingSettings.isEnabled() && currentTemperature != null) {
            if (currentTemperature < 100) {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 100), 1 << 6, false, 4, 0);
            } else {
                breweryInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 10), 1 << 5, false, 4, 0);
            }
        } else {
            breweryInterface.displayClear(0);
        }
    }

    public void calibrateTemperature(Integer side, Float value) {
        Configuration configuration = configProvider.loadConfiguration();

        configuration = updateTemperatureCalibrationMeasurements(configuration, side, value);

        configuration = updateTemperatureCalibration(configuration);

        configProvider.saveConfiguration(configuration);
    }

    private Configuration updateTemperatureCalibration(Configuration configuration) {
        return configuration;
        // TODO
//        List<Float> measurements =
//                configuration.getTemperatureCalibrationMeasurements().get(getUsedSensorId(configuration));
//
//        Configuration.ConfigurationBuilder configurationBuilder = configuration.toBuilder();
//
//        if (measurements.stream().filter(Objects::nonNull).count() != 4) {
//            configurationBuilder.temperatureCalibration(Map.of());
//        } else {
//            Map<String, List<Float>> currCalibration = configuration.getTemperatureCalibration();
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
//            List<Float> currCalibrations = Arrays.asList(a, b);
//            // TODO
//            currCalibration.put(getUsedSensorId(configuration), currCalibrations);
//            configurationBuilder.temperatureCalibration(currCalibration);
//        }
//
//        return configurationBuilder.build();
    }

    private Configuration updateTemperatureCalibrationMeasurements(Configuration configuration, Integer side,
                                                                   Float value) {
        Map<String, List<Float>> allMeasurements;
        if (configuration.getTemperatureCalibrationMeasurements() == null) {
            allMeasurements = new HashMap<>();
        } else {
            allMeasurements = new HashMap<>(configuration.getTemperatureCalibrationMeasurements());
        }

        // TODO:
//        String brewingSensorId = getUsedSensorId(configuration);
//        List<Float> currMeasurements;
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

        Configuration.ConfigurationBuilder configurationBuilder = configuration.toBuilder();
        configurationBuilder.temperatureCalibrationMeasurements(allMeasurements);

        return configurationBuilder.build();
    }

    private boolean isAlarmEnabled(Float currentTemperature) {
        return brewingSettings.isEnabled() && brewingSettings.isTemperatureAlarmEnabled() && brewingSettings.getDestinationTemperature() != null
                && currentTemperature != null && currentTemperature >= brewingSettings.getDestinationTemperature();
    }

    private void driveMotor(Configuration configuration) {
        if (configuration.getBrewingMotorNumber() != null) {
            breweryInterface.motorEnable(configuration.getBrewingMotorNumber(),
                    brewingSettings.isEnabled() && brewingSettings.isMotorEnabled());
        }
    }

    private void setMainsPower(Float currentTemperature) {
        if (brewingSettings.isEnabled() && currentTemperature != null
                && brewingSettings.getDestinationTemperature() != null
                && currentTemperature < brewingSettings.getDestinationTemperature()
        ) {
            int driveMaxPower = 0xff;
            if (brewingSettings.getMaxPower() != null) {
                driveMaxPower = (int) (brewingSettings.getMaxPower() / 100.0 * 0xff);
            }

            int drivePower = 0xff;
            if (brewingSettings.getPowerTemperatureCorrelation() != null) {
                drivePower = (int) (
                        (brewingSettings.getDestinationTemperature() - currentTemperature) * brewingSettings.getPowerTemperatureCorrelation()
                );
            }

            if (drivePower > driveMaxPower) {
                drivePower = driveMaxPower;
            } else if (drivePower < 0) {
                drivePower = 0;
            }

            breweryInterface.setMainsPower(1, drivePower);
            breweryInterface.setMainsPower(2, drivePower);
        } else {
            breweryInterface.setMainsPower(1, 0);
            breweryInterface.setMainsPower(2, 0);
        }
    }

}
