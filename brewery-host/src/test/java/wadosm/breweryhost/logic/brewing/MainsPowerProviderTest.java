package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.device.driver.BreweryInterface;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MainsPowerProviderTest {

    @ParameterizedTest
    @CsvSource({
            ", , , 0",
            "50.0, , , 0",
            ", 25.0, , 0",
            "50.0, 25.0, , 255",
            "50.0, 25.0, 100, 255",
            "50.0, 25.0, 50, 127",
            "50.0, 25.0, 0, 0",
    })
    void verify_scenarios_fixed_temp_correlation(Float destinationTemperature, Float currentTemperature, Integer maxPower, int expectedPower) {
        // given
        BrewingSettingsProviderImpl brewingSettingsProvider = getMockedBrewingSettingsProvider(BrewingSettings.builder().
                enabled(true).
                destinationTemperature(destinationTemperature).
                maxPower(maxPower).
                build());

        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        MainsPowerProvider powerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        // when
        powerProvider.updatePowerForTemperature(currentTemperature);

        // then
        verify(breweryInterface).setMainsPower(1, expectedPower);
        verify(breweryInterface).setMainsPower(2, expectedPower);
    }

    @ParameterizedTest
    @CsvSource({
            ", , , 0",
            "50.0, , , 0",
            ", 25.0, , 0",
            "50.0, 25.0, , 255",
            "50.0, 25.0, 1, 63",
            "50.0, 25.0, 0.5, 31",
            "100.0, 0.0, 5, 255",
            "10.0, 100.0, 1, 0",
    })
    void verify_scenarios_vary_temp_correlation(Float destinationTemperature, Float currentTemperature, Float powerTemperatureCorrelation, int expectedPower) {
        // given
        BrewingSettingsProviderImpl brewingSettingsProvider = getMockedBrewingSettingsProvider(BrewingSettings.builder().
                enabled(true).
                destinationTemperature(destinationTemperature).
                powerTemperatureCorrelation(powerTemperatureCorrelation).
                build());

        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        MainsPowerProvider powerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        // when
        powerProvider.updatePowerForTemperature(currentTemperature);

        // then
        verify(breweryInterface).setMainsPower(1, expectedPower);
        verify(breweryInterface).setMainsPower(2, expectedPower);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 1.0, , 50.0, 50",
            "100, 1.0, 10, 50.0, 10",
    })
    void verify_expected_power(Float destinationTemperature, Float powerTemperatureCorrelation, Integer maxPower, Float currentTemperature, Integer expectedPower) {
        // given
        BrewingSettingsProviderImpl brewingSettingsProvider = getMockedBrewingSettingsProvider(BrewingSettings.builder()
                .enabled(true)
                .destinationTemperature(destinationTemperature)
                .powerTemperatureCorrelation(powerTemperatureCorrelation)
                .maxPower(maxPower)
                .build());

        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        MainsPowerProvider powerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        // when
        powerProvider.updatePowerForTemperature(currentTemperature);

        // then
        assertThat(powerProvider.getCurrentPower()).isEqualTo(expectedPower);
    }

    @Test
    void should_disable_power_after_update_below_threshold() {
        // given
        BrewingSettingsProviderImpl brewingSettingsProvider = getMockedBrewingSettingsProvider(BrewingSettings.builder()
                .enabled(true)
                .destinationTemperature(50.0f)
                .build());

        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        MainsPowerProvider powerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        // when
        powerProvider.updatePowerForTemperature(0.0f);
        powerProvider.updatePowerForTemperature(60.0f);

        // then
        assertThat(powerProvider.getCurrentPower()).isEqualTo(0);
    }

    private static BrewingSettingsProviderImpl getMockedBrewingSettingsProvider(BrewingSettings brewingSettings) {
        BrewingSettingsProviderImpl settingsProvider = mock(BrewingSettingsProviderImpl.class);
        when(settingsProvider.getBrewingSettings()).thenReturn(brewingSettings);
        return settingsProvider;
    }
}