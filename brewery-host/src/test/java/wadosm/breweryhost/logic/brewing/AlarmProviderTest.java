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

        BrewingSettingsProvider settingsProvider = mock(BrewingSettingsProvider.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(BrewingSettings.builder().build());

        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, settingsProvider, mockConfigProvider(), mock(TimeProvider.class));

        // when
        alarmProvider.handleAlarm(currentTemperature);

        // then
        verify(breweryInterface).setAlarm(false);
    }

    @Test
    void alarm_enabled_after_threshold() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettingsProvider settingsProvider = mock(BrewingSettingsProvider.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build()
        );

        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, settingsProvider, mockConfigProvider(), mock(TimeProvider.class));

        // when
        alarmProvider.handleAlarm(51.0);

        // then
        verify(breweryInterface).setAlarm(true);
    }

    private static ConfigProvider mockConfigProvider() {
        ConfigProvider configurationProvider = mock(ConfigProvider.class);
        when(configurationProvider.loadConfiguration()).thenReturn(Configuration.builder().build());
        return configurationProvider;
    }

    @Test
    void alarm_off_after_specified_period() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettingsProvider settingsProvider = mock(BrewingSettingsProvider.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build()
        );

        ConfigProvider configurationProvider = mock(ConfigProvider.class);
        when(configurationProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, settingsProvider, configurationProvider, timeProvider);

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
    void timeout_reset_after_alarm_off() {
        // given
        BreweryInterface breweryInterface = mock(BreweryInterface.class);

        BrewingSettingsProvider settingsProvider = mock(BrewingSettingsProvider.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(BrewingSettings.builder()
                .enabled(true)
                .temperatureAlarmEnabled(true)
                .destinationTemperature(50.0)
                .build()
        );

        ConfigProvider configurationProvider = mock(ConfigProvider.class);
        when(configurationProvider.loadConfiguration()).thenReturn(Configuration.builder()
                .alarmMaxTime(Duration.ofSeconds(2))
                .build()
        );

        TimeProvider timeProvider = mock(TimeProvider.class);
        AlarmProvider alarmProvider = new AlarmProvider(breweryInterface, settingsProvider, configurationProvider, timeProvider);

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
}