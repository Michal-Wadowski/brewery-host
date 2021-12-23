package wadosm.breweryhost.device.externalinterface;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import wadosm.breweryhost.device.SocketWrapper;
import wadosm.breweryhost.device.driver.DriverInterfaceImpl;
import wadosm.breweryhost.device.externalinterface.dto.ResponseDTO;
import wadosm.breweryhost.logic.DeviceCommand;
import wadosm.breweryhost.logic.DeviceResponse;
import wadosm.breweryhost.logic.brewing.BrewingController;
import wadosm.breweryhost.logic.brewing.BrewingState;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriverSessionImplTest {

    private final ObjectMapper objectMapper;

    public DriverSessionImplTest() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void sendResponse() throws JSONException {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);
        BrewingState brewingState = new BrewingState(true, 72.0f, 90.0f, 50, 0.2f, 1234, false, true, 90);
        ResponseDTO responseDTO = new BrewingController.BrewingStatusResponse(123, 3650L, brewingState);

        // when
        driverSession.sendResponse(responseDTO);

        // then
        String lastWritten = socketWrapper.getLastWritten();
        JSONAssert.assertEquals("{\"responseDto\": {\"commandId\":123, \"time\": 3650}}", lastWritten, false);

        JSONAssert.assertEquals("{\"responseDto\": {\"brewingState\":{" +
                "\"enabled\": true," +
                "\"currentTemperature\": 72," +
                "\"destinationTemperature\": 90," +
                "\"maxPower\": 50," +
                "\"powerTemperatureCorrelation\": 0.2," +
                "\"timeElapsed\": 1234," +
                "\"motorEnabled\": false," +
                "\"temperatureAlarm\": true," +
                "\"heatingPower\": 90" +
                "}}}", lastWritten, false);
    }

    @Test
    void sendCommand() throws JSONException {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);

        // when
        driverSession.sendCommand(
                new DeviceCommand("softPwmCreate", Arrays.asList(DriverInterfaceImpl.Pin.POWER, 0, 2815))
        );

        // then
        String lastWritten = socketWrapper.getLastWritten();
        JSONAssert.assertEquals("{\"commands\": [{" +
                "\"function\": \"softPwmCreate\"," +
                "\"arguments\": [" + DriverInterfaceImpl.Pin.POWER.getPinNumber() + ", 0, 2815]" +
                "}]}", lastWritten, false);
    }

    @Test
    void sendCommand_gets_response() throws JSONException {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);

        // when
        DeviceResponse deviceResponse = driverSession.sendCommand(new DeviceCommand("softPwmCreate", List.of()));

        // then
        assertThat(deviceResponse).isNotNull();
        assertThat(deviceResponse.getFunction()).isNotBlank();
        assertThat(deviceResponse.getArguments()).hasSizeGreaterThan(0);
    }

    @Test
    void sendCommands() throws JSONException {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);

        // when
        driverSession.sendCommands(List.of(
                new DeviceCommand("foo", Arrays.asList(1, 2)),
                new DeviceCommand("bar", Arrays.asList(3, 4))
        ));

        // then
        String lastWritten = socketWrapper.getLastWritten();
        JSONAssert.assertEquals("{\"commands\": [{" +
                "\"function\": \"foo\"," +
                "\"arguments\": [1, 2]" +
                "}, {" +
                "\"function\": \"bar\"," +
                "\"arguments\": [3, 4]" +
                "}]}", lastWritten, false);
    }

    @Test
    void sendCommands_gets_response() {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);

        // when
        List<DeviceResponse> deviceResponses = driverSession.sendCommands(List.of(
                new DeviceCommand("foo", Arrays.asList(1, 2)),
                new DeviceCommand("bar", Arrays.asList(3, 4))
        ));

        // then
        assertThat(deviceResponses).isNotNull().isNotEmpty();
        DeviceResponse deviceResponse = deviceResponses.get(0);
        assertThat(deviceResponse.getFunction()).isNotBlank();
        assertThat(deviceResponse.getArguments()).hasSizeGreaterThan(0);
    }

    @Test
    void sendCommands_should_handle_null_data_read() {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);
        socketWrapper.setCustomResponse(null);

        // when
        List<DeviceResponse> deviceResponses = driverSession.sendCommands(List.of(
                new DeviceCommand("foo", Arrays.asList(1, 2)),
                new DeviceCommand("bar", Arrays.asList(3, 4))
        ));

        // then
        assertThat(deviceResponses).isNotNull().isEmpty();
    }

    @Test
    void sendCommands_should_handle_invalid_data_read() {
        // given
        FakeSocketWrapper socketWrapper = new FakeSocketWrapper();
        DriverSessionImpl driverSession = new DriverSessionImpl(socketWrapper, objectMapper);
        socketWrapper.setCustomResponse("{");

        // when
        List<DeviceResponse> deviceResponses = driverSession.sendCommands(List.of(
                new DeviceCommand("foo", Arrays.asList(1, 2)),
                new DeviceCommand("bar", Arrays.asList(3, 4))
        ));

        // then
        assertThat(deviceResponses).isNotNull().isEmpty();
    }

    private static class FakeSocketWrapper implements SocketWrapper {

        Deque<String> written = new LinkedList<>();

        String customResponse = null;
        boolean customResponseSet = false;

        @Override
        public String read() {
            if (customResponseSet) {
                return customResponse;
            } else {
                return "[{" +
                        "\"function\": \"softPwmCreate\"," +
                        "\"arguments\": [32, 0, 2815]," +
                        "\"response\": null" +
                        "}]";
            }
        }

        public void setCustomResponse(String customResponse) {
            this.customResponse = customResponse;
            customResponseSet = true;
        }

        @Override
        public void write(String data) {
            written.add(data);
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        public String getLastWritten() {
            return written.pollFirst();
        }
    }
}