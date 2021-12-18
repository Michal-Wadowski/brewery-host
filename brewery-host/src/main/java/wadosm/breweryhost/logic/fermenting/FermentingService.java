package wadosm.breweryhost.logic.fermenting;

import wadosm.breweryhost.logic.DeviceCommand;

import java.util.List;

public interface FermentingService {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    FermentingState getFermentingState();

}
