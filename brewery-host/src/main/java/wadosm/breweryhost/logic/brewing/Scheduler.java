package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@EnableAsync
public class Scheduler {

    private final BrewingService brewingService;
    private final TimeProvider timeProvider;

    private final ConfigProvider configProvider;
    private boolean enabled = false;
    private Instant startWhen;
    private Instant endWhen;

    private Boolean started;

    @Async
    @Scheduled(fixedRateString = "${brewing.checkingPeriod}")
    public void processStep() {
        if (!enabled) {
            return;
        }

        if (getCurrStepIndex() == null || getCurrStepIndex() < 0) {
            setCurrStepIndex(0);
        }

        Instant currentTime = timeProvider.getCurrentTime();

        if (started != null && endWhen != null) {
            if (currentTime.isAfter(endWhen) || currentTime.equals(endWhen)) {
                changeCurrStep(getCurrStepIndex() + 1);
            }
        }

        if (getCurrStepIndex() < getBrewingSchedule().getScheduleSteps().size()) {

            BrewingSchedule.ScheduleStep currStep = getBrewingSchedule().getScheduleSteps().get(getCurrStepIndex());

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

    public void changeCurrStep(int currStepIndex) {
        started = null;
        startWhen = null;
        endWhen = null;

        setCurrStepIndex(currStepIndex);
    }

    private void setCurrStepIndex(int currStepIndex) {
        configProvider.updateAndSaveConfiguration(configuration -> configuration
                .withBrewingSchedule(configuration
                        .getBrewingSchedule()
                        .withCurrStepIndex(currStepIndex)
                )
        );
    }

    private Integer getCurrStepIndex() {
        return getBrewingSchedule().getCurrStepIndex();
    }

    private BrewingSchedule getBrewingSchedule() {
        return configProvider.loadConfiguration().getBrewingSchedule();
    }

    public void enable(boolean enable) {
        if (enabled != enable) {
            enabled = enable;
            startWhen = null;
            endWhen = null;
            started = null;
            if (!enable) {
                brewingService.setAlarmMode(AlarmMode.THRESHOLD_TRIGGERED);
            }
        }
    }
}
