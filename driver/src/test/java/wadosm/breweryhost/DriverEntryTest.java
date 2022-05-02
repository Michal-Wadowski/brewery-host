package wadosm.breweryhost;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriverEntryTest {

    @Test
    void say_hello() {
        DriverEntry driverEntry = new DriverEntry();
        driverEntry.sayHello();
    }
}