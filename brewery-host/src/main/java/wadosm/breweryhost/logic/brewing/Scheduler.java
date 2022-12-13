package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;

import java.time.Instant;

// TODO: Store scheduler status in configuration in case device restart
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final BrewingService brewingService;
    private final TimeProvider timeProvider;

    private Integer currStepIndex;
    private Instant startWhen;
    private Instant endWhen;

    private boolean enabled = false;
    private Boolean started;

    public void processStep(BrewingSchedule brewingSchedule) {
        if (!enabled) {
            return;
        }

        if (currStepIndex == null || currStepIndex < 0) {
            currStepIndex = 0;
        }

        Instant currentTime = timeProvider.getCurrentTime();

        if (started != null && endWhen != null) {
            if (currentTime.isAfter(endWhen) || currentTime.equals(endWhen)) {
                started = null;
                currStepIndex++;
                startWhen = null;
                endWhen = null;
            }
        }

        if (currStepIndex < brewingSchedule.getScheduleSteps().size()) {

            BrewingSchedule.ScheduleStep currStep = brewingSchedule.getScheduleSteps().get(currStepIndex);

            if (currStep.getStartAfter() != null && startWhen == null) {
                startWhen = currentTime.plus(currStep.getStartAfter());
            }

            if (currStep.getDuration() != null && endWhen == null) {
                endWhen = currentTime.plus(currStep.getDuration());

                if (currStep.getStartAfter() != null) {
                    endWhen = endWhen.plus(currStep.getStartAfter());
                }
            }

            if (started == null) {
                if (startWhen != null) {
                    if (currentTime.isAfter(startWhen) || currentTime.equals(startWhen)) {
                        started = false;
                    }
                } else {
                    started = false;
                }

                if (Boolean.FALSE.equals(started)) {
                    if (currStep.getAlarm() != null) {
                        if (currStep.getTemperature() == null) {
                            brewingService.setAlarmMode(AlarmMode.MANUAL);
                        } else {
                            brewingService.setAlarmMode(AlarmMode.THRESHOLD_TRIGGERED);
                        }
                        brewingService.enableAlarm(currStep.getAlarm());
                    }

                    if (currStep.getTemperature() != null) {
                        brewingService.setDestinationTemperature(currStep.getTemperature());
                    }

                    if (currStep.getMotor() != null) {
                        brewingService.motorEnable(currStep.getMotor());
                    }

                    if (currStep.getMaxPower() != null) {
                        brewingService.setMaxPower(currStep.getMaxPower());
                    }

                    if (currStep.getPowerTemperatureCorrelation() != null) {
                        brewingService.setPowerTemperatureCorrelation(currStep.getPowerTemperatureCorrelation());
                    }

                    started = true;
                }
            }
        }
    }

    public void enable(boolean enable) {
        enabled = enable;
        if (!enable) {
            brewingService.setAlarmMode(AlarmMode.THRESHOLD_TRIGGERED);
        }
    }
}
