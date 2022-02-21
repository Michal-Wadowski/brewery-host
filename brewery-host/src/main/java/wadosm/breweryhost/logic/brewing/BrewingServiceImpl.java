package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceState;
import wadosm.breweryhost.device.temperature.TemperatureProvider;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.Configuration;

import java.util.*;

@Service
@Log4j2
@EnableAsync
public class BrewingServiceImpl implements BrewingService {

    private final DriverInterface driverInterface;

    private final TemperatureProvider temperatureProvider;
    private final ConfigProvider configProvider;

    @Value("${brewing.temperature_sensor.id}")
    @Getter
    @Setter
    private String brewingTemperatureSensor;

    @Value("${brewing.motor_number}")
    @Getter
    @Setter
    private Integer motorNumber;

    private boolean enabled;
    private Float destinationTemperature;
    private Integer maxPower;
    private Float temperatureCorrelation;
    private boolean motorEnabled;
    private boolean temperatureAlarmEnabled;

    public BrewingServiceImpl(
            DriverInterface driverInterface,
            TemperatureProvider temperatureProvider,
            ConfigProvider configProvider) {
        this.driverInterface = driverInterface;
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
    public void setTimer(int seconds) {

    }

    @Override
    public void removeTimer() {

    }

    @Override
    public void motorEnable(boolean enable) {
        motorEnabled = enable;
        processStep();
    }

    @Override
    public BrewingState getBrewingState() {
        Integer heatingPower = getHeatingPower();
        return new BrewingState(
                enabled, getCurrentTemperature(), destinationTemperature, maxPower, getPowerTemperatureCorrelation(),
                null, motorEnabled, temperatureAlarmEnabled, heatingPower
        );
    }

    private Integer getHeatingPower() {
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();
        if (driverInterfaceState != null) {
            return (int) (driverInterfaceState.getMains(1) * 100.0 / 0xff);
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

    private Float getUncalibratedTemperature() {
        Integer rawValue = temperatureProvider.getSensorTemperature(brewingTemperatureSensor);

        if (rawValue != null) {
            return rawValue / 1000.0f;
        } else {
            return null;
        }
    }

    private Float getCurrentTemperature() {
        Float temperature = getUncalibratedTemperature();

        if (temperature != null) {
            List<Float> temperatureCalibration = configProvider.getTemperatureCalibrationOf(brewingTemperatureSensor);
            if (temperatureCalibration != null && temperatureCalibration.size() == 2) {
                temperature *= (1 + temperatureCalibration.get(0));
                temperature += temperatureCalibration.get(1);
            }
            return Math.round(temperature * 100) / 100.0f;
        } else {
            return null;
        }
    }

    @Async
    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    public void processStep() {
        Float currentTemperature = getCurrentTemperature();

        setMainsPower(currentTemperature);

        driveMotor();

        driverInterface.setAlarm(isAlarmEnabled(currentTemperature));

        displayTemperature(currentTemperature);
    }

    private void displayTemperature(Float currentTemperature) {
        if (enabled) {
            if (currentTemperature < 100) {
                driverInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 100), 1 << 6, false, 4, 0);
            } else {
                driverInterface.displayShowNumberDecEx(0, (int) (currentTemperature * 10), 1 << 5, false, 4, 0);
            }
        } else {
            driverInterface.displayClear(0);
        }
    }

    public void calibrateTemperature(Integer side, Float value) {
        Configuration configuration = configProvider.getConfiguration();

        updateTemperatureCalibrationMeasurements(configuration, side, value);

        updateTemperatureCalibration(configuration);

        configProvider.setConfiguration(configuration);
    }

    private void updateTemperatureCalibration(Configuration configuration) {
        List<Float> measurements = configuration.getTemperatureCalibrationMeasurements().get(brewingTemperatureSensor);

        if (measurements.stream().filter(Objects::nonNull).count() != 4) {
            configuration.setTemperatureCalibration(Map.of());
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
            currCalibration.put(brewingTemperatureSensor, currCalibrations);
            configuration.setTemperatureCalibration(currCalibration);
        }
    }

    private void updateTemperatureCalibrationMeasurements(Configuration configuration,
            Integer side, Float value) {

        Map<String, List<Float>> allMeasurements = configuration.getTemperatureCalibrationMeasurements();
        if (allMeasurements == null) {
            allMeasurements = Map.of();
        }
        allMeasurements = new HashMap<>(allMeasurements);

        List<Float> currMeasurements;
        if (!allMeasurements.containsKey(brewingTemperatureSensor) ||
                allMeasurements.get(brewingTemperatureSensor).size() != 4
        ) {
            currMeasurements = Arrays.asList(null, null, null, null);
        } else {
            currMeasurements = allMeasurements.get(brewingTemperatureSensor);
        }

        if (side == 0) {
            currMeasurements.set(0, getUncalibratedTemperature());
            currMeasurements.set(1, value);
        } else if (side == 1) {
            currMeasurements.set(2, getUncalibratedTemperature());
            currMeasurements.set(3, value);
        }

        allMeasurements.put(brewingTemperatureSensor, currMeasurements);
        configuration.setTemperatureCalibrationMeasurements(allMeasurements);
    }

    private boolean isAlarmEnabled(Float currentTemperature) {
        return enabled && temperatureAlarmEnabled && destinationTemperature != null
                && currentTemperature != null && currentTemperature >= destinationTemperature;
    }

    private void driveMotor() {
        driverInterface.motorEnable(motorNumber, enabled && motorEnabled);
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

            driverInterface.setMainsPower(1, drivePower);
            driverInterface.setMainsPower(2, drivePower);
        } else {
            driverInterface.setMainsPower(1, 0);
            driverInterface.setMainsPower(2, 0);
        }
    }
}
