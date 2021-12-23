package wadosm.breweryhost.logic.fermenting;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

public interface FermentingService {

    void enable(boolean enable);

    void setDestinationTemperature(Float temperature);

    FermentingState getFermentingState();

    @Async
    @Scheduled(fixedRateString = "${fermenting.checkingPeriod}")
    void processStep();
}
