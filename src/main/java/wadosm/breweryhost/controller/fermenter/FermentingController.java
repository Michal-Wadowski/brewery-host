package wadosm.breweryhost.controller.fermenter;

public interface FermentingController {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    FermentingStatus getFermentingStatus();
}
