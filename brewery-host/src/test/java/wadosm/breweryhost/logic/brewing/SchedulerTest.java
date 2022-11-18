package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SchedulerTest {

    @Test
    void empty_scheduler_does_noting() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder().brewingSchedule(
                BrewingSchedule.builder().build()
        ).build());

        Scheduler scheduler = new Scheduler(brewingService, configProvider, new TimeProvider());

        // when
        scheduler.processStep();

        // then
        verify(brewingService, never()).setDestinationTemperature(any());
        verify(brewingService, never()).setPowerTemperatureCorrelation(any());
        verify(brewingService, never()).setMaxPower(any());
        verify(brewingService, never()).enableTemperatureAlarm(anyBoolean());
        verify(brewingService, never()).motorEnable(anyBoolean());
        verify(brewingService, never()).setAlarmMode(any());
    }

    @Test
    void alarm_after_specified_delay() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder().brewingSchedule(
                BrewingSchedule.builder().scheduleSteps(List.of(
                        BrewingSchedule.ScheduleStep.builder()
                                .startAfter(Duration.ofSeconds(1))
                                .alarm(true)
                                .build()
                )).build()
        ).build());

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = new Scheduler(brewingService, configProvider, timeProvider);

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1900));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2100));
        scheduler.processStep();

        // then
        verify(brewingService, atLeastOnce()).setAlarmMode(AlarmMode.ALWAYS_RUNNING);

        ArgumentCaptor<Boolean> alarmSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(brewingService, atLeastOnce()).enableTemperatureAlarm(alarmSequence.capture());

        assertThat(alarmSequence.getAllValues()).containsSequence(false, false, true);
    }
}