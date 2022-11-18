package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlarmProviderTest {

    static Stream<Arguments> alarm_disabled_by_default() {
        return Stream.of(
                Arguments.of(null, BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(true)
                        .destinationTemperature(50.0)
                        .build()),

                Arguments.of(49.0, BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(true)
                        .destinationTemperature(50.0)
                        .build()),

                Arguments.of(100.0, BrewingSettings.builder()
                        .enabled(false)
                        .temperatureAlarmEnabled(true)
                        .destinationTemperature(50.0)
                        .build()),

                Arguments.of(100.0, BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(false)
                        .destinationTemperature(50.0)
                        .build()),

                Arguments.of(100.0, BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(true)
                        .destinationTemperature(null)
                        .build()),

                Arguments.of(100.0, BrewingSettings.builder().build())
        );
    }

    @ParameterizedTest
    @MethodSource
    void alarm_disabled_by_default(Double currentTemperature) {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        ConfigProvider configProvider = mockConfigProvider(BrewingSettings.builder().build());
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, mock(TimeProvider.class), new TemperatureThresholdTriggerImpl(configProvider));

        // when
        alarmProvider.handleAlarm(currentTemperature);

        // then
        verify(breweryInterface).setAlarm(false);
    }

    @Test
    void alarm_enabled_after_threshold() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettings brewingSettings = BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build();

        ConfigProvider configProvider = mockConfigProvider(brewingSettings);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, mock(TimeProvider.class), new TemperatureThresholdTriggerImpl(configProvider));

        // when
        alarmProvider.handleAlarm(51.0);

        // then
        verify(breweryInterface).setAlarm(true);
    }

    @Test
    void alarm_off_after_specified_period() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettings brewingSettings = BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build();

        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .brewingSettings(brewingSettings)
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, timeProvider, new TemperatureThresholdTriggerImpl(configProvider));

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1500));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(2500));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(3500));
        alarmProvider.handleAlarm(51.0);

        // then
        ArgumentCaptor<Boolean> alarmSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(breweryInterface, atLeastOnce()).setAlarm(alarmSequence.capture());

        assertThat(alarmSequence.getAllValues()).containsSequence(true, true, true, false);
    }

    @Test
    void alarm_off_after_specified_period_with_dummy_trigger() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettings brewingSettings = BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .build();

        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .brewingSettings(brewingSettings)
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, timeProvider, new DummyTriggerImpl());

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        alarmProvider.handleAlarm(null);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(3500));
        alarmProvider.handleAlarm(null);

        // then
        ArgumentCaptor<Boolean> alarmSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(breweryInterface, atLeastOnce()).setAlarm(alarmSequence.capture());

        assertThat(alarmSequence.getAllValues()).containsSequence(true, false);
    }

    @Test
    void should_count_down_time_after_alarmEnabled() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .brewingSettings(BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(false)
                        .destinationTemperature(50.0)
                        .build())
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, timeProvider, new TemperatureThresholdTriggerImpl(configProvider));

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        alarmProvider.handleAlarm(51.0);

        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .brewingSettings(BrewingSettings.builder()
                        .enabled(true)
                        .temperatureAlarmEnabled(true)
                        .destinationTemperature(50.0)
                        .build())
                .build()
        );

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(5000));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(7500));
        alarmProvider.handleAlarm(51.0);

        // then
        ArgumentCaptor<Boolean> alarmSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(breweryInterface, atLeastOnce()).setAlarm(alarmSequence.capture());

        assertThat(alarmSequence.getAllValues()).containsSequence(true, false);
    }

    @Test
    void timeout_reset_after_alarm_off() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettings brewingSettings = BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build();

        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .brewingSettings(brewingSettings)
                .alarmMaxTime(Duration.ofSeconds(2))
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, configProvider, timeProvider, new TemperatureThresholdTriggerImpl(configProvider));

        // when
        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(1000));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(3500));
        alarmProvider.handleAlarm(49.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(4500));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(6400));
        alarmProvider.handleAlarm(51.0);

        when(timeProvider.getCurrentTime()).thenReturn(Instant.ofEpochMilli(6600));
        alarmProvider.handleAlarm(51.0);

        // then
        ArgumentCaptor<Boolean> alarmSequence = ArgumentCaptor.forClass(Boolean.class);
        verify(breweryInterface, atLeastOnce()).setAlarm(alarmSequence.capture());

        assertThat(alarmSequence.getAllValues()).containsSequence(true, false, true, true, false);
    }

    private static ConfigProvider mockConfigProvider(BrewingSettings brewingSettings) {
        ConfigProvider configurationProvider = mock(ConfigProvider.class);
        when(configurationProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .brewingSettings(brewingSettings)
                .build());
        return configurationProvider;
    }
}