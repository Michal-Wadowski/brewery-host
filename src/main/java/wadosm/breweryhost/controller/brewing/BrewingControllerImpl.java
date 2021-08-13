package wadosm.breweryhost.controller.brewing;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.controller.fermenter.FermentingStatus;
import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.externalinterface.CommandListener;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.externalinterface.dto.CommandDTO;
import wadosm.breweryhost.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.temperature.TemperatureProvider;

import java.time.Instant;

@Service
@Log4j2
public class BrewingControllerImpl implements BrewingController, CommandListener {

    private final DriverInterface driverInterface;

    private final TemperatureProvider temperatureProvider;

    private final ExternalInterface externalInterface;

    private boolean enabled;
    private Float destinationTemperature;
    private Integer maxPower;
    private Float temperatureCorrelation;

    @Value("${brewing.temperature_sensor.id}")
    @Getter
    @Setter
    private String brewingTemperatureSensor;

    public BrewingControllerImpl(
            DriverInterface driverInterface,
            TemperatureProvider temperatureProvider,
            ExternalInterface externalInterface
    ) {
        this.driverInterface = driverInterface;
        this.temperatureProvider = temperatureProvider;
        this.externalInterface = externalInterface;

        this.externalInterface.addCommandListener(this);
    }

    @Override
    public void enable(boolean enable) {
        this.enabled = enable;
    }

    @Override
    public void setDestinationTemperature(Float temperature) {
        if (temperature == null || temperature > 0 && temperature < 100) {
            destinationTemperature = temperature;
        }
    }

    @Override
    public void setMaxPower(Integer powerInPercents) {
        this.maxPower = powerInPercents;
    }

    @Override
    public void setPowerTemperatureCorrelation(Float percentagesPerDegree) {
        if (percentagesPerDegree != null) {
            this.temperatureCorrelation = (float) (percentagesPerDegree / 100.0 * 0xff);
        } else {
            this.temperatureCorrelation = null;
        }
    }

    @Override
    public void setTimer(int seconds) {

    }

    @Override
    public void removeTimer() {

    }

    @Override
    public void motorEnable(boolean enable) {

    }

    @Override
    public FermentingStatus getBrewingStatus() {
        return null;
    }

    private Float getCurrentTemperature() {
        Integer result = temperatureProvider.getSensorTemperature(brewingTemperatureSensor);

        if (result != null) {
            return result / 1000.0f;
        } else {
            return null;
        }
    }

    @Scheduled(fixedRate = 1000)
    public void processStep() {
        Float currentTemperature = getCurrentTemperature();
        if (driverInterface.lock()) {
            if (enabled && currentTemperature != null
                    && destinationTemperature != null
                    && currentTemperature < destinationTemperature
            ) {
                int driveMaxPower = 0xff;
                if (this.maxPower != null) {
                    driveMaxPower = (int)(this.maxPower / 100.0 * 0xff);
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

            driverInterface.unlock();
        }
    }

    @Override
    public void commandReceived(CommandDTO commandDTO) {
        log.info(commandDTO);
        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_getBrewingStatus) {
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setDestinationTemperature) {
            Float value = commandDTO.getFloatValue();
            setDestinationTemperature(value);
            processStep();
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_enable) {
            Boolean enable = commandDTO.getEnable();
            enable(enable);
            processStep();
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setMaxPower) {
            Integer maxPower = commandDTO.getIntValue();
            setMaxPower(maxPower);
            processStep();
            sendStatusResponse(commandDTO);
        }

        if (commandDTO.getCommand() == CommandDTO.Command.Brewing_setPowerTemperatureCorrelation) {
            Float temperatureCorrelation = commandDTO.getFloatValue();
            setPowerTemperatureCorrelation(temperatureCorrelation);
            processStep();
            sendStatusResponse(commandDTO);
        }
    }

    private void sendStatusResponse(CommandDTO commandDTO) {
        FermentingStatus brewingStatus = getBrewingStatus();
        externalInterface.sendResponse( new BrewingStatusResponse(
                commandDTO.getCommandId(), Instant.now().getEpochSecond(), brewingStatus
        ));
    }

    @Getter
    private static class BrewingStatusResponse extends ResponseDTO {

        private final FermentingStatus brewingStatus;

        public BrewingStatusResponse(Integer commandId, Long time, FermentingStatus brewingStatus) {
            super(commandId, time);
            this.brewingStatus = brewingStatus;
        }
    }
}
