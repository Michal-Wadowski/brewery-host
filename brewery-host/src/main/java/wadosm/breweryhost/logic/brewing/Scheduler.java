package wadosm.breweryhost.logic.brewing;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;

import java.util.List;

@Component
@AllArgsConstructor
public class Scheduler {

    private final BrewingService brewingService;
    private final ConfigProvider configProvider;
    private final TimeProvider timeProvider;

    private Integer currStepIndex;

    public void processStep() {
        if (currStepIndex != null) {
            List<BrewingSchedule.ScheduleStep> steps = configProvider.loadConfiguration().getBrewingSchedule().getScheduleSteps();

        }
    }
}
