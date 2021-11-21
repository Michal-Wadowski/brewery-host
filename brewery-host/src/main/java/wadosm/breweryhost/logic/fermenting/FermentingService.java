package wadosm.breweryhost.logic.fermenting;

public interface FermentingService {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    FermentingState getFermentingState();

}
