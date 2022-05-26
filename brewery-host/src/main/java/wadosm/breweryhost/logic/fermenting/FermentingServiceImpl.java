package wadosm.breweryhost.logic.fermenting;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.device.driver.BreweryState;
import wadosm.breweryhost.device.temperature.TemperatureProvider;

// TODO: Implement saving task on disk
@Service
@Log4j2
@EnableAsync
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

    private final BreweryInterface breweryInterface;

    private final TemperatureProvider temperatureProvider;

    public FermentingServiceImpl(
            BreweryInterface breweryInterface,
            TemperatureProvider temperatureProvider
    ) {
        this.breweryInterface = breweryInterface;
        this.temperatureProvider = temperatureProvider;
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
        BreweryState breweryState = breweryInterface.readDriverInterfaceState();
        return breweryState.getMotor(motorNumber);
    }

    @Async
    @Scheduled(fixedRateString = "${fermenting.checkingPeriod}")
    @Override
    public void processStep() {
        Float currentTemperature = getCurrentTemperature();

        if (enabled && currentTemperature != null && destinationTemperature != null) {
            breweryInterface.motorEnable(motorNumber, currentTemperature < destinationTemperature);
        } else {
            breweryInterface.motorEnable(motorNumber, false);
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
