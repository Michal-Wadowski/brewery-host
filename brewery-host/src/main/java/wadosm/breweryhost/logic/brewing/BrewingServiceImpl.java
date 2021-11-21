package wadosm.breweryhost.logic.brewing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.DriverInterfaceState;
import wadosm.breweryhost.device.externalinterface.ExternalInterface;
import wadosm.breweryhost.device.temperature.TemperatureProvider;

@Service
@Log4j2
public class BrewingServiceImpl implements BrewingService {

    private final DriverInterface driverInterface;

    private final TemperatureProvider temperatureProvider;

    @Value("${brewing.temperature_sensor.id}")
    @Getter
    @Setter
    private String brewingTemperatureSensor;

    @Value("${brewing.motor_number}")
    @Getter
    @Setter
    private Integer motorNumber;

    @Value("${brewing.alarmHi}")
    @Getter
    @Setter
    private Integer alarmHi;

    @Value("${brewing.alarmLo}")
    @Getter
    @Setter
    private Integer alarmLo;

    private boolean enabled;
    private Float destinationTemperature;
    private Integer maxPower;
    private Float temperatureCorrelation;
    private boolean motorEnabled;
    private int soundTone = 0;
    private boolean temperatureAlarmEnabled;
    private boolean soundEnabled;

    public BrewingServiceImpl(
            DriverInterface driverInterface,
            TemperatureProvider temperatureProvider,
            ExternalInterface externalInterface
    ) {
        this.driverInterface = driverInterface;
        this.temperatureProvider = temperatureProvider;
    }

    @Override
    public void enable(boolean enable) {
        this.enabled = enable;
        processStep();
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        if (temperature == null || temperature > 0 && temperature < 100) {
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
    public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
        if (percentagesPerDegree != null) {
            this.temperatureCorrelation = (float) (percentagesPerDegree / 100.0 * 0xff);
        } else {
            this.temperatureCorrelation = null;
        }
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
        return new BrewingState(
                enabled, getCurrentTemperature(), destinationTemperature, maxPower, getPowerTemperatureCorrelation(),
                null, motorEnabled, temperatureAlarmEnabled, getHeatingPower()
        );
    }

    private Integer getHeatingPower() {
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();
        return (int) (driverInterfaceState.getMains(1) * 100.0 / 0xff);
    }

    private Float getPowerTemperatureCorrelation() {
        if (temperatureCorrelation != null) {
            return temperatureCorrelation / 0xff * 100;
        } else {
            return null;
        }
    }

    private Float getCurrentTemperature() {
        Integer result = temperatureProvider.getSensorTemperature(brewingTemperatureSensor);

        if (result != null) {
            return result / 1000.0f;
        } else {
            return null;
        }
    }

    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    public void processStep() {
        Float currentTemperature = getCurrentTemperature();

        if (driverInterface.lock()) {
            setMainsPower(currentTemperature);

            driveMotor();

            soundEnabled = enabled && temperatureAlarmEnabled && destinationTemperature != null
                    && currentTemperature != null && currentTemperature >= destinationTemperature;

            driverInterface.unlock();
        }
    }

    @Scheduled(fixedRateString = "${brewing.soundPeriod}")
    public void soundStep() {
        if (driverInterface.lock()) {
            playSound();
            driverInterface.unlock();
        }
    }

    private void driveMotor() {
        driverInterface.motorEnable(motorNumber, enabled && motorEnabled);
    }

    private void playSound() {
        if (soundEnabled) {
            if (soundTone == 0) {
                soundTone = 1;
                driverInterface.playSound(alarmHi);
            } else {
                soundTone = 0;
                driverInterface.playSound(alarmLo);
            }
        } else {
            driverInterface.playSound(0);
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

            driverInterface.setMainsPower(1, drivePower);
            driverInterface.setMainsPower(2, drivePower);
        } else {
            driverInterface.setMainsPower(1, 0);
            driverInterface.setMainsPower(2, 0);
        }
    }
}
