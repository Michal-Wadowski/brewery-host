package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import wadosm.breweryhost.logic.brewing.dto.EnableDto;
import wadosm.breweryhost.logic.brewing.dto.SetCurrentStepDto;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;

import javax.validation.Valid;

@Log4j2
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final ConfigProvider configProvider;

    private final Scheduler scheduler;

    @GetMapping("/schedule")
    public BrewingSchedule getSchedule() {
        return configProvider.loadConfiguration().getBrewingSchedule();
    }

    @PostMapping("/schedule")
    public void updateSchedule(@Valid @RequestBody BrewingSchedule brewingSchedule) {
        configProvider.updateAndSaveConfiguration(configuration -> configuration.withBrewingSchedule(brewingSchedule));
    }

    @PostMapping("/enable")
    public void enable(@Valid @RequestBody EnableDto enable) {
        scheduler.enable(enable.getEnable());
    }

    @PostMapping("/currentStep")
    public void setCurrentStep(@Valid @RequestBody SetCurrentStepDto currentStepDto) {
        scheduler.changeCurrStep(currentStepDto.getCurrentStep());
    }
}
