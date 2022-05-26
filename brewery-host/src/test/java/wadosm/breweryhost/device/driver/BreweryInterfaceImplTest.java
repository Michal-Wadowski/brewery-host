package wadosm.breweryhost.device.driver;

import org.junit.jupiter.api.Test;
import wadosm.breweryhost.DigiPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class BreweryInterfaceImplTest {

    @Test
    void should_init_device_using_driver() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterfaceImpl driverInterface = new BreweryInterfaceImpl(digiPort);

        // when
        driverInterface.initDriver();

        // then
        // Randomly check initials
        verify(digiPort).digitalWrite(BreweryInterfaceImpl.Pin.POWER.pinNumber, 1);
        verify(digiPort).softPwmCreate(BreweryInterfaceImpl.Pin.MAINS_1.pinNumber, 0, 0x0a * 0xff);
        verify(digiPort).displayInit(0, BreweryInterfaceImpl.Pin.SPI1_CLK.pinNumber,
                BreweryInterfaceImpl.Pin.SPI1_DIO.pinNumber);
    }

    @Test
    void readDriverInterfaceState_should_send_read_commands() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        BreweryInterfaceImpl driverInterface = new BreweryInterfaceImpl(digiPort);

        // when
        driverInterface.readDriverInterfaceState();

        // then
        verify(digiPort, times(4)).digitalRead(anyInt());
        verify(digiPort, times(2)).softPwmRead(anyInt());

        // Randomly check calls
        verify(digiPort).digitalRead(BreweryInterfaceImpl.Pin.MOTOR_1.pinNumber);
        verify(digiPort).softPwmRead(BreweryInterfaceImpl.Pin.MAINS_2.pinNumber);
    }

    @Test
    void readDriverInterfaceState_should_process_response() {
        // given
        DigiPort digiPort = mock(DigiPort.class);
        when(digiPort.digitalRead(BreweryInterfaceImpl.Pin.POWER.pinNumber)).thenReturn(1);
        when(digiPort.digitalRead(BreweryInterfaceImpl.Pin.MOTOR_1.pinNumber)).thenReturn(1);
        when(digiPort.digitalRead(BreweryInterfaceImpl.Pin.MOTOR_2.pinNumber)).thenReturn(0);
        when(digiPort.digitalRead(BreweryInterfaceImpl.Pin.MOTOR_3.pinNumber)).thenReturn(0);
        when(digiPort.softPwmRead(BreweryInterfaceImpl.Pin.MAINS_1.pinNumber)).thenReturn(100 * 0xa);
        when(digiPort.softPwmRead(BreweryInterfaceImpl.Pin.MAINS_2.pinNumber)).thenReturn(50 * 0xa);

        BreweryInterfaceImpl driverInterface = new BreweryInterfaceImpl(digiPort);

        // when
        BreweryState breweryState = driverInterface.readDriverInterfaceState();

        // then
        assertThat(breweryState).isNotNull();

        assertThat(breweryState.getPower()).isEqualTo(true);
        assertThat(breweryState.getMotor1()).isEqualTo(true);
        assertThat(breweryState.getMotor2()).isEqualTo(false);
        assertThat(breweryState.getMotor3()).isEqualTo(false);
        assertThat(breweryState.getMains1()).isEqualTo(100);
        assertThat(breweryState.getMains2()).isEqualTo(50);
    }
}