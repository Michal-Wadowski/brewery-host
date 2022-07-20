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
import wadosm.breweryhost.logic.brewing.model.BrewingState;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@EnableAsync
public class BrewingServiceImpl implements BrewingService {

    private final BreweryInterface breweryInterface;

    private final TemperatureSensorProvider temperatureSensorProvider;
    private final ConfigProvider configProvider;

    private boolean enabled;
    private Float destinationTemperature;
    private Integer maxPower;
    private Float temperatureCorrelation;
    private boolean motorEnabled;
    private boolean temperatureAlarmEnabled;
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
        this.enabled = enable;
        processStep();
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        if (temperature == null || temperature >= 0 && temperature <= 100) {
            destinationTemperature = temperature;
            processStep();
        }
    }

    @Override
    public void enableTemperatureAlarm(boolean enable) {
        temperatureAlarmEnabled = enable;
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        this.maxPower = powerInPercents;
        processStep();
    }

    @Override
    public void motorEnable(boolean enable) {
        motorEnabled = enable;
        processStep();
    }

    @Override
    public BrewingState getBrewingState() {
        Configuration configuration = configProvider.loadConfiguration();

        Integer heatingPower = getHeatingPower();
        return new BrewingState(
                enabled, getCurrentTemperature(configuration), destinationTemperature, maxPower,
                getPowerTemperatureCorrelation(), motorEnabled, temperatureAlarmEnabled, heatingPower
        );
    }

    private Integer getHeatingPower() {
        BreweryState breweryState = breweryInterface.readDriverInterfaceState();
        if (breweryState != null) {
            return (int) (breweryState.getMains(1) * 100.0 / 0xff);
        }
        return null;
    }

    private Float getPowerTemperatureCorrelation() {
        if (temperatureCorrelation != null) {
            return temperatureCorrelation / 0xff * 100;
        } else {
            return null;
        }
    }

    @Override
    public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
        if (percentagesPerDegree != null) {
            this.temperatureCorrelation = (float) (percentagesPerDegree / 100.0 * 0xff);
        } else {
            this.temperatureCorrelation = null;
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
        if (enabled && currentTemperature != null) {
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
        return enabled && temperatureAlarmEnabled && destinationTemperature != null
                && currentTemperature != null && currentTemperature >= destinationTemperature;
    }

    private void driveMotor(Configuration configuration) {
        if (configuration.getBrewingMotorNumber() != null) {
            breweryInterface.motorEnable(configuration.getBrewingMotorNumber(), enabled && motorEnabled);
        }
    }

    private void setMainsPower(Float currentTemperature) {
        if (enabled && currentTemperature != null
                && destinationTemperature != null
                && currentTemperature < destinationTemperature
        ) {
            int driveMaxPower = 0xff;
            if (this.maxPower != null) {
                driveMaxPower = (int) (this.maxPower / 100.0 * 0xff);
            }

            int drivePower = 0xff;
            if (temperatureCorrelation != null) {
                drivePower = (int) ((destinationTemperature - currentTemperature) * temperatureCorrelation);
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
