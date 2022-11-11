package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wadosm.breweryhost.device.driver.BreweryInterface;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        BrewingSettingsProvider brewingSettingsProvider = new BrewingSettingsProvider();
        brewingSettingsProvider.getBrewingSettings().setEnabled(true);
        brewingSettingsProvider.getBrewingSettings().setDestinationTemperature(destinationTemperature);
        brewingSettingsProvider.getBrewingSettings().setMaxPower(maxPower);

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
        BrewingSettingsProvider brewingSettingsProvider = new BrewingSettingsProvider();
        brewingSettingsProvider.getBrewingSettings().setEnabled(true);
        brewingSettingsProvider.getBrewingSettings().setDestinationTemperature(destinationTemperature);
        brewingSettingsProvider.getBrewingSettings().setPowerTemperatureCorrelation(powerTemperatureCorrelation);

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
        BrewingSettingsProvider brewingSettingsProvider = new BrewingSettingsProvider();
        brewingSettingsProvider.getBrewingSettings().setEnabled(true);
        brewingSettingsProvider.getBrewingSettings().setDestinationTemperature(destinationTemperature);
        brewingSettingsProvider.getBrewingSettings().setPowerTemperatureCorrelation(powerTemperatureCorrelation);
        brewingSettingsProvider.getBrewingSettings().setMaxPower(maxPower);

        BreweryInterface breweryInterface = mock(BreweryInterface.class);
        MainsPowerProvider powerProvider = new MainsPowerProvider(brewingSettingsProvider, breweryInterface);

        // when
        powerProvider.updatePowerForTemperature(currentTemperature);

        // then
        assertThat(powerProvider.getCurrentPower()).isEqualTo(expectedPower);
    }
}