package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.general.ConfigProvider;

@Component
@AllArgsConstructor
public class Scheduler {

    private final BrewingService brewingService;
    private final ConfigProvider configProvider;
    private final TimeProvider timeProvider;

    public void processStep() {

    }
}
