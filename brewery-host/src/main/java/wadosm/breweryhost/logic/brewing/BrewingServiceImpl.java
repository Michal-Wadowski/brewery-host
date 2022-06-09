package wadosm.breweryhost.logic.brewing;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.device.driver.BreweryState;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.Configuration;

import java.util.*;

@Service
@Log4j2
@EnableAsync
public class BrewingServiceImpl implements BrewingService {

    private final BreweryInterface breweryInterface;

    private final TemperatureProvider temperatureProvider;
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
            TemperatureProvider temperatureProvider,
            ConfigProvider configProvider) {
        this.breweryInterface = breweryInterface;
        this.temperatureProvider = temperatureProvider;
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
                getPowerTemperatureCorrelation(), null, motorEnabled, temperatureAlarmEnabled, heatingPower
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

    private Float getUncalibratedTemperature(Configuration configuration) {
        Integer rawValue = temperatureProvider.getSensorTemperature(configuration.getBrewingSensorId());

        if (rawValue != null) {
            return rawValue / 1000.0f;
        } else {
            return null;
        }
    }

    private Float getCurrentTemperature(Configuration configuration) {
        Float temperature = getUncalibratedTemperature(configuration);

        if (temperature != null) {
            Map<String, List<Float>> temperatureCalibration = configuration.getTemperatureCalibration();

            if (temperatureCalibration != null && temperatureCalibration.containsKey(configuration.getBrewingSensorId())) {
                List<Float> sensorCalibration = temperatureCalibration.get(configuration.getBrewingSensorId());
                if (sensorCalibration.size() == 2) {
                    temperature *= (1 + sensorCalibration.get(0));
                    temperature += sensorCalibration.get(1);
                }
            }

            return Math.round(temperature * 100) / 100.0f;
        } else {
            return null;
        }
    }

    @Async
    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    public void processStep() {
        Configuration configuration = configProvider.loadConfiguration();

        Float currentTemperature = getCurrentTemperature(configuration);

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
        List<Float> measurements =
                configuration.getTemperatureCalibrationMeasurements().get(configuration.getBrewingSensorId());

        Configuration.ConfigurationBuilder configurationBuilder = configuration.toBuilder();

        if (measurements.stream().filter(Objects::nonNull).count() != 4) {
            configurationBuilder.temperatureCalibration(Map.of());
        } else {
            Map<String, List<Float>> currCalibration = configuration.getTemperatureCalibration();
            if (currCalibration == null) {
                currCalibration = Map.of();
            }
            currCalibration = new HashMap<>(currCalibration);

            var x1 = measurements.get(0);
            var x2 = measurements.get(2);
            var t1 = measurements.get(1);
            var t2 = measurements.get(3);
            var a = (t2 - t1) / (x2 - x1);
            var b = -a * x1 + t1;

            List<Float> currCalibrations = Arrays.asList(a, b);
            currCalibration.put(configuration.getBrewingSensorId(), currCalibrations);
            configurationBuilder.temperatureCalibration(currCalibration);
        }

        return configurationBuilder.build();
    }

    private Configuration updateTemperatureCalibrationMeasurements(Configuration configuration, Integer side,
                                                                   Float value) {
        Map<String, List<Float>> allMeasurements;
        if (configuration.getTemperatureCalibrationMeasurements() == null) {
            allMeasurements = new HashMap<>();
        } else {
            allMeasurements = new HashMap<>(configuration.getTemperatureCalibrationMeasurements());
        }

        List<Float> currMeasurements;
        if (!allMeasurements.containsKey(configuration.getBrewingSensorId()) ||
                allMeasurements.get(configuration.getBrewingSensorId()).size() != 4
        ) {
            currMeasurements = Arrays.asList(null, null, null, null);
        } else {
            currMeasurements = allMeasurements.get(configuration.getBrewingSensorId());
        }

        if (side == 0) {
            currMeasurements.set(0, getUncalibratedTemperature(configuration));
            currMeasurements.set(1, value);
        } else if (side == 1) {
            currMeasurements.set(2, getUncalibratedTemperature(configuration));
            currMeasurements.set(3, value);
        }

        allMeasurements.put(configuration.getBrewingSensorId(), currMeasurements);

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
