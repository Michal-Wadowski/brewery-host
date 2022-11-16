package wadosm.breweryhost.logic.brewing;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.logic.brewing.model.BrewingSchedule;
import wadosm.breweryhost.logic.general.ConfigProvider;
import wadosm.breweryhost.logic.general.model.Configuration;

import static org.mockito.Mockito.*;

class SchedulerTest {

    @Test
    void name() {
        // given
        BrewingService brewingService = mock(BrewingService.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(configProvider.loadConfiguration()).thenReturn(Configuration.builder().brewingSchedule(
                BrewingSchedule.builder()
                        .build()
        ).build());

        Scheduler scheduler = new Scheduler(brewingService, configProvider, new TimeProvider());

        // when
        scheduler.processStep();

        // then
        verify(brewingService, never()).setDestinationTemperature(any());
        verify(brewingService, never()).setPowerTemperatureCorrelation(any());
        verify(brewingService, never()).setMaxPower(any());
//        verify(brewingService, never()).enableTemperatureAlarm(any());
        verify(brewingService, never()).motorEnable(anyBoolean());
    }
}