package wadosm.breweryhost.logic.general;

import org.springframework.scheduling.annotation.Scheduled;

public interface UpdateService {
    @Scheduled(fixedRateString = "${update.checkingPeriod}")
    void checkUpdates();
}
