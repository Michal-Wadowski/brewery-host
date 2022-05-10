package wadosm.breweryhost;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class DriverEntryTest {

    @Test
    void check_DigiPort_methods() {
        DigiPort digiPort = new DigiPort(new DriverEntryImpl());

        digiPort.pinMode(1, 2);
        digiPort.softPwmCreate(1, 2, 3);
        digiPort.digitalWrite(1, 2);
        digiPort.softPwmStop(1);
        digiPort.softPwmWrite(1, 2);

        assertThat(digiPort.digitalRead(1)).isEqualTo(2);
        assertThat(digiPort.softPwmRead(1)).isEqualTo(2);

        digiPort.displayInit(1, 2, 3);
        digiPort.setBrightness(1, 2, true);
        digiPort.setSegments(1, new byte[] {0x02, 0x03}, 2, 3);
        digiPort.clear(1);
        digiPort.showNumberDec(1, 2, true, 4, 5);
        digiPort.showNumberDecEx(1, 2, 3, true, 5, 6);
        digiPort.showNumberHexEx(1, 2, 3, true, 5, 6);
    }

    @Test
    void check_ConnectionConsumer() {
        ConnectionConsumer connectionConsumer = new ConnectionConsumer(
                new DriverEntryImpl(),
                message -> {
                    System.out.println("ConnectionConsumer.receivedMessage(): " + message);
                    return "#MESSAGE RESPONSE#";
                }
        );

        connectionConsumer.attachListener();
    }
}