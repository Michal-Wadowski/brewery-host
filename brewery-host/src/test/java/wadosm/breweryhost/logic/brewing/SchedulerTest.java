package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static wadosm.breweryhost.logic.brewing.AlarmMode.MANUAL;
import static wadosm.breweryhost.logic.brewing.AlarmMode.THRESHOLD_TRIGGERED;

class SchedulerTest {

    @Test
    void reset_alarm_after_scheduler_disable() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().build();
        TimeProvider timeProvider = mock(TimeProvider.class);

        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when
        scheduler.enable(false);

        // then
        verify(brewingService, never()).setDestinationTemperature(any());
        verify(brewingService, never()).setPowerTemperatureCorrelation(any());
        verify(brewingService, never()).setMaxPower(any());
        verify(brewingService, never()).enableAlarm(anyBoolean());
        verify(brewingService, never()).motorEnable(anyBoolean());
        verify(brewingService, atLeastOnce()).setAlarmMode(THRESHOLD_TRIGGERED);
    }

    @Test
    void scheduler_does_not_execute_if_not_enabled() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = getAlarmSchedule();
        TimeProvider timeProvider = mock(TimeProvider.class);

        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1900));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2100));
        scheduler.processStep();

        // then
        verify(brewingService, never()).setDestinationTemperature(any());
        verify(brewingService, never()).setPowerTemperatureCorrelation(any());
        verify(brewingService, never()).setMaxPower(any());
        verify(brewingService, never()).enableAlarm(anyBoolean());
        verify(brewingService, never()).motorEnable(anyBoolean());
        verify(brewingService, never()).setAlarmMode(any());
    }

    @Test
    void set_alarm_after_specified_delay() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = getAlarmSchedule();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2000));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2900));
        scheduler.processStep();

        // then 1
        verify(brewingService, never()).setAlarmMode(any());
        verify(brewingService, never()).enableAlarm(anyBoolean());

        // when 2
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(3100));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).setAlarmMode(MANUAL);
        verify(brewingService, times(1)).enableAlarm(true);
    }

    @Test
    void set_temperature_and_alarm_after_specified_delay() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .startAfter(Duration.ofSeconds(1))
                        .temperature(70.0)
                        .alarm(true)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1900));
        scheduler.processStep();

        // then 1
        verify(brewingService, never()).setAlarmMode(any());
        verify(brewingService, never()).enableAlarm(anyBoolean());

        // when 2
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2100));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).setAlarmMode(THRESHOLD_TRIGGERED);
        verify(brewingService, times(1)).enableAlarm(true);
        verify(brewingService, times(1)).setDestinationTemperature(70.0);
    }

    @Test
    void set_temperature_and_alarm_immediately() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .temperature(70.0)
                        .alarm(true)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).setAlarmMode(THRESHOLD_TRIGGERED);
        verify(brewingService, times(1)).enableAlarm(true);
        verify(brewingService, times(1)).setDestinationTemperature(70.0);
    }

    @Test
    void set_temperature_immediately() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .temperature(70.0)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // then 2
        verify(brewingService, never()).setAlarmMode(any());
        verify(brewingService, never()).enableAlarm(anyBoolean());
        verify(brewingService, times(1)).setDestinationTemperature(70.0);
    }

    @Test
    void temperatures_sequence() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .startAfter(Duration.ofSeconds(1))
                        .duration(Duration.ofSeconds(2))
                        .temperature(70.0)
                        .alarm(true)
                        .build(),

                BrewingSchedule.ScheduleStep.builder()
                        .duration(Duration.ofSeconds(2))
                        .temperature(85.0)
                        .alarm(false)
                        .build(),

                BrewingSchedule.ScheduleStep.builder()
                        .duration(Duration.ofSeconds(2))
                        .alarm(true)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // when 2
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2001));
        scheduler.processStep();

        // when 3
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(4002));
        scheduler.processStep();

        // when 4
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(5003));
        scheduler.processStep();

        // when 5
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(6003));
        scheduler.processStep();

        // then
        ArgumentCaptor<AlarmMode> alarmModeSequence = ArgumentCaptor.forClass(AlarmMode.class);
        verify(brewingService, atLeastOnce()).setAlarmMode(alarmModeSequence.capture());

        ArgumentCaptor<Boolean> alarmEnableSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(brewingService, atLeastOnce()).enableAlarm(alarmEnableSequence.capture());

        ArgumentCaptor<Double> temperatureSequence = ArgumentCaptor.forClass(Double.class);
        verify(brewingService, atLeastOnce()).setDestinationTemperature(temperatureSequence.capture());

        assertThat(alarmModeSequence.getAllValues()).containsSequence(THRESHOLD_TRIGGERED, THRESHOLD_TRIGGERED, MANUAL);
        assertThat(alarmEnableSequence.getAllValues()).containsSequence(true, false, true);
        assertThat(temperatureSequence.getAllValues()).containsSequence(70.0, 85.0);
    }

    @Test
    void set_motor_immediately() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .motor(true)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).motorEnable(true);
    }

    @Test
    void set_maxPower_immediately() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .maxPower(70)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).setMaxPower(70);
    }

    @Test
    void set_powerTemperatureCorrelation_immediately() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        BrewingSchedule brewingSchedule = BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .powerTemperatureCorrelation(1.5)
                        .build()
        )).build();

        TimeProvider timeProvider = mock(TimeProvider.class);
        Scheduler scheduler = getScheduler(brewingService, brewingSchedule, timeProvider);

        // when 1
        scheduler.enable(true);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        scheduler.processStep();

        // then 2
        verify(brewingService, times(1)).setPowerTemperatureCorrelation(1.5);
    }

    private static Scheduler getScheduler(BrewingService brewingService, BrewingSchedule brewingSchedule, TimeProvider timeProvider) {
        ConfigProvider configProvider = new ConfigProvider() {

            private Configuration configuration = Configuration.builder().brewingSchedule(brewingSchedule).build();

            @Override
            public Configuration loadConfiguration() {
                return configuration;
            }

            @Override
            public void saveConfiguration(Configuration configuration) {
                this.configuration = configuration;
            }

            @Override
            public void updateAndSaveConfiguration(Function<Configuration, Configuration> updateConfiguration) {
                this.configuration = updateConfiguration.apply(configuration);
            }
        };

        return new Scheduler(brewingService, timeProvider, configProvider);
    }

    private static BrewingSchedule getAlarmSchedule() {
        return BrewingSchedule.builder().scheduleSteps(List.of(
                BrewingSchedule.ScheduleStep.builder()
                        .startAfter(Duration.ofSeconds(1))
                        .alarm(true)
                        .build()
        )).build();
    }

}
