package wadosm.breweryhost.controller.fermenter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.driver.DriverInterfaceState;
import wadosm.breweryhost.externalinterface.CommandListener;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.temperature.TemperatureProvider;

import java.time.Instant;

// TODO: Implement saving task on disk
@Service
@Log4j2
public class FermentingControllerImpl implements FermentingController, CommandListener {

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

    private final ExternalInterface externalInterface;

    public FermentingControllerImpl(
            TemperatureProvider temperatureProvider, DriverInterface driverInterface,
            ExternalInterface externalInterface
    ) {
        this.temperatureProvider = temperatureProvider;
        this.driverInterface = driverInterface;
        this.externalInterface = externalInterface;

        externalInterface.addCommandListener(this);
    }

    @Override
    public void enable(boolean enable) {
        enabled = enable;
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        if (temperature == null || temperature > 0 && temperature < 100) {
            destinationTemperature = temperature;
        }
    }

    @Override
    public FermentingStatus getFermentingStatus() {
        return new FermentingStatus(enabled, getCurrentTemperature(), destinationTemperature, getHeating());
    }

    private boolean getHeating() {
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();
        return driverInterfaceState.getMotor(motorNumber);
    }

    @Scheduled(fixedRate = 1000)
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

    @Getter
    private static class FermentingStatusResponse extends ResponseDTO {

        private final FermentingStatus fermentingStatus;

        public FermentingStatusResponse(Integer commandId, Long time, FermentingStatus fermentingStatus) {
            super(commandId, time);
            this.fermentingStatus = fermentingStatus;
        }
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        log.info(commandDTO);
        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_getFermentingStatus) {
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            setDestinationTemperature(value);
            processStep();
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Fermenting_enable) {
            Boolean enable = commandDTO.getEnable();
            enable(enable);
            processStep();
            sendStatusResponse(commandDTO);
        }
    }

    private void sendStatusResponse(CommandDTO commandDTO) {
        FermentingStatus fermentingStatus = getFermentingStatus();
        externalInterface.sendResponse( new FermentingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), fermentingStatus
        ));
    }

}
