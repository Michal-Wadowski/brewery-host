package wadosm.breweryhost.device.driver;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wadosm.breweryhost.MessagesProcessor;
import wadosm.breweryhost.device.externalinterface.DriverSession;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.logic.DeviceCommand;
import wadosm.breweryhost.logic.DeviceResponse;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: Fix tests
@Disabled
class DriverInterfaceImplTest {

    @Test
    void readDriverInterfaceState_if_session_not_set() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);

        // when
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();

        // then
        assertThat(driverInterface.isReady()).isFalse();
        assertThat(driverInterfaceState).isNull();
    }

    @Test
    void readDriverInterfaceState_should_set_not_ready_after_remove_session() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);
        driverInterface.setSession(new FakeSession());

        // when
        driverInterface.removeSession();

        // then
        assertThat(driverInterface.isReady()).isFalse();
    }

    @Test
    void should_init_driver_after_session_established() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);
        FakeSession session = new FakeSession();

        // when
        driverInterface.setSession(session);

        // then
        assertThat(driverInterface.isReady()).isTrue();
        assertThat(session.commandsSent).hasSizeGreaterThan(0);

        // Randomly check initials
        assertThat(session.commandsSent.stream().filter(x -> x.equals(
                new DeviceCommand("pinMode", List.of(DriverInterfaceImpl.Pin.POWER, 1))
        )).count()).isEqualTo(1);

        assertThat(session.commandsSent.stream().filter(x -> x.equals(
                new DeviceCommand("softPwmCreate", List.of(DriverInterfaceImpl.Pin.MAINS_1, 0, 0x0a * 0xff))
        )).count()).isEqualTo(1);

        assertThat(session.commandsSent.stream().filter(x -> x.equals(
                new DeviceCommand(
                        "displayInit",
                        List.of(0, DriverInterfaceImpl.Pin.SPI1_CLK, DriverInterfaceImpl.Pin.SPI1_DIO)
                )
        )).count()).isEqualTo(1);
    }

    @Test
    void readDriverInterfaceState_should_send_read_commands() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);
        FakeSession session = new FakeSession();
        driverInterface.setSession(session);
        session.commandsSent.clear();

        // when
        driverInterface.readDriverInterfaceState();

        // then
        assertThat(session.commandsSent).hasSize(6);

        // Randomly check initials
        assertThat(session.commandsSent.stream().filter(x -> x.equals(
                new DeviceCommand("digitalRead", List.of(DriverInterfaceImpl.Pin.MOTOR_1))
        )).count()).isEqualTo(1);

        assertThat(session.commandsSent.stream().filter(x -> x.equals(
                new DeviceCommand("softPwmRead", List.of(DriverInterfaceImpl.Pin.MAINS_2))
        )).count()).isEqualTo(1);
    }

    @Test
    void readDriverInterfaceState_should_process_response() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);
        FakeSession session = new FakeSession();
        driverInterface.setSession(session);
        session.commandsSent.clear();

        // when
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();

        // then
        assertThat(driverInterfaceState).isNotNull();

        assertThat(driverInterfaceState.getPower()).isEqualTo(true);
        assertThat(driverInterfaceState.getMotor1()).isEqualTo(true);
        assertThat(driverInterfaceState.getMotor2()).isEqualTo(false);
        assertThat(driverInterfaceState.getMotor3()).isEqualTo(false);
        assertThat(driverInterfaceState.getMains1()).isEqualTo(100);
        assertThat(driverInterfaceState.getMains2()).isEqualTo(50);
    }

    @Test
    void readDriverInterfaceState_should_returns_null_when_invalid_response_response() {
        // given
        DriverInterfaceImpl driverInterface = new DriverInterfaceImpl(message -> null);
        FakeSession session = new FakeSession();
        driverInterface.setSession(session);
        session.commandsSent.clear();
        session.expectedResponse.remove(0);

        // when
        DriverInterfaceState driverInterfaceState = driverInterface.readDriverInterfaceState();

        // then
        assertThat(driverInterfaceState).isNull();
    }

    private static class FakeSession implements DriverSession {

        final List<DeviceResponse> expectedResponse = new LinkedList<>(List.of(
                new DeviceResponse("digitalRead", List.of(DriverInterfaceImpl.Pin.POWER.getPinNumber()), 1),
                new DeviceResponse("digitalRead", List.of(DriverInterfaceImpl.Pin.MOTOR_1.getPinNumber()), 1),
                new DeviceResponse("digitalRead", List.of(DriverInterfaceImpl.Pin.MOTOR_2.getPinNumber()), 0),
                new DeviceResponse("digitalRead", List.of(DriverInterfaceImpl.Pin.MOTOR_3.getPinNumber()), 0),

                new DeviceResponse("softPwmRead", List.of(DriverInterfaceImpl.Pin.MAINS_1.getPinNumber()), 0x0a * 100),
                new DeviceResponse("softPwmRead", List.of(DriverInterfaceImpl.Pin.MAINS_2.getPinNumber()), 0x0a * 50)
        ));
        List<DeviceCommand> commandsSent = new ArrayList<>();

        @Override
        public void sendResponse(ResponseDTO responseDTO) {

        }

        @Override
        public DeviceResponse sendCommand(DeviceCommand command) {
            List<DeviceResponse> deviceResponses = sendCommands(List.of(command));
            return deviceResponses.get(0);
        }

        @Override
        public List<DeviceResponse> sendCommands(List<DeviceCommand> commandList) {
            commandsSent.addAll(commandList);
            return expectedResponse;
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }
}