package wadosm.breweryhost;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverController implements BreweryInterface {

    DriverInterface driverInterface;
    TemperatureSensorsReader temperatureSensorsReader;
    SystemServices systemServices;

    public DriverController(DriverInterface driverInterface,
                            TemperatureSensorsReader temperatureSensorsReader,
                            SystemServices systemServices
    ) {
        this.driverInterface = driverInterface;
        this.temperatureSensorsReader = temperatureSensorsReader;
        this.systemServices = systemServices;
    }

    @Override
    public BreweryStatus readBreweryStatus() {
        BreweryStatus status = driverInterface.readBreweryStatus();
        List<TemperatureSensor> sensorList = temperatureSensorsReader.readSensors();
        if (sensorList.size() > 0) {
            status.setTemperature1(sensorList.get(0).getTemperature());
        }
        if (sensorList.size() > 1) {
            status.setTemperature1(sensorList.get(1).getTemperature());
        }
        return status;
    }

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
        driverInterface.powerEnable(enable);
    }

    @Override
    public void motorEnable(int motorNumber, boolean enable) {
        driverInterface.motorEnable(motorNumber, enable);
    }

    @Override
    public void playSound(int period) {
        driverInterface.playSound(period);
    }

    @Override
    public void setMainsPower(int mainsNumber, int power) {
        driverInterface.setMainsPower(mainsNumber, power);
    }
}
