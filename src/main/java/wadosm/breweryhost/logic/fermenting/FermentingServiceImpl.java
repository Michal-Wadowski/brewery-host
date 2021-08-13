package wadosm.breweryhost.logic.fermenting;

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

// TODO: Implement saving task on disk
@Service
@Log4j2
public class FermentingServiceImpl implements FermentingService {

    @Value("${fermenting.temperature_sensor.id}")
    @Getter
    @Setter
    private String fermentingTemperatureSensor;

    @Value("${fermenting.motor_number}")
    @Getter
    @Setter
    private Integer motorNumber;

    private boolean enabled;
    private Float destinationTemperature;

    private final TemperatureProvider temperatureProvider;

    private final DriverInterface driverInterface;

    public FermentingServiceImpl(
            TemperatureProvider temperatureProvider, DriverInterface driverInterface,
            ExternalInterface externalInterface
    ) {
        this.temperatureProvider = temperatureProvider;
        this.driverInterface = driverInterface;
    }

    @Override
    public void enable(boolean enable) {
        enabled = enable;

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
    public FermentingState getFermentingState() {
        return new FermentingState(enabled, getCurrentTemperature(), destinationTemperature, getHeating());
    }

    private boolean getHeating() {
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();
        return driverInterfaceState.getMotor(motorNumber);
    }

    @Scheduled(fixedRateString = "${fermenting.checkingPeriod}")
    public void processStep() {
        Float currentTemperature = getCurrentTemperature();
        if (driverInterface.lock()) {
            if (enabled && currentTemperature != null && destinationTemperature != null) {
                driverInterface.motorEnable(motorNumber, currentTemperature < destinationTemperature);
            } else {
                driverInterface.motorEnable(motorNumber, false);
            }
            driverInterface.unlock();
        }
    }

    private Float getCurrentTemperature() {
        Integer result = temperatureProvider.getSensorTemperature(fermentingTemperatureSensor);

        if (result != null) {
            return result / 1000.0f;
        } else {
            return null;
        }
    }

}
