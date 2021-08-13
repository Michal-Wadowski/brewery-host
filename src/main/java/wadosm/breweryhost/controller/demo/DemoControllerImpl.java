package wadosm.breweryhost.controller.demo;

import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.externalinterface.ExternalInterface;
import wadosm.breweryhost.system.SystemServices;
import wadosm.breweryhost.temperature.TemperatureSensor;
import wadosm.breweryhost.temperature.TemperatureSensorsReader;

import java.util.List;

//@Service
public class DemoControllerImpl implements DemoController {

    private final DriverInterface driverInterface;
    private final TemperatureSensorsReader temperatureSensorsReader;
    private final SystemServices systemServices;
    private final ExternalInterface externalInterface;

    List<TemperatureSensor> temperatureSensors;

    public DemoControllerImpl(DriverInterface driverInterface,
                              TemperatureSensorsReader temperatureSensorsReader,
                              SystemServices systemServices,
                              ExternalInterface externalInterface
    ) {
        this.driverInterface = driverInterface;
        this.temperatureSensorsReader = temperatureSensorsReader;
        this.systemServices = systemServices;
        this.externalInterface = externalInterface;

//        externalInterface.addCommandListener(this::processCommand);
    }

//    @Override
//    public BreweryStatusDTO readBreweryStatus() {
//        DriverInterfaceState interfaceState = driverInterface.readDriverInterfaceState();
//
//        return new BreweryStatusDTO(null, Instant.now().getEpochSecond(), interfaceState, temperatureSensors);
//    }

    @Override
    public void doReboot() {
        systemServices.doReboot();
    }

    @Override
    public void doPowerOff() {
        systemServices.doPowerOff();
    }

    @Override
    public void powerEnable(boolean enable) {
        driverInterface.lock();
        driverInterface.powerEnable(enable);
        driverInterface.unlock();
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        driverInterface.lock();
        driverInterface.motorEnable(motorNumber, enable);
        driverInterface.unlock();
    }

    @Override
    public void playSound(int period) {
        driverInterface.lock();
        driverInterface.playSound(period);
        driverInterface.unlock();
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        driverInterface.lock();
        driverInterface.setMainsPower(mainsNumber, power);
        driverInterface.unlock();
    }

//    private void processCommand(CommandDTO commandDTO) {
//        switch (commandDTO.getCommand()) {
//
//            case "playSound":
//                playSound(commandDTO.getValue());
//                break;
//
//            case "powerOff":
//                doPowerOff();
//                break;
//
//            case "readBreweryStatus":
//                BreweryStatusDTO breweryStatusDTO = readBreweryStatus();
//                externalInterface.sendBreweryStatus(breweryStatusDTO);
//                break;
//        }
//    }
}
