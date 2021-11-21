package wadosm.breweryhost.integrationtests.logic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wadosm.breweryhost.device.driver.DriverInterface;
import wadosm.breweryhost.device.driver.FakeDriverInterface;
import wadosm.breweryhost.logic.brewing.BrewingController;
import wadosm.breweryhost.logic.brewing.BrewingService;
import wadosm.breweryhost.logic.fermenting.FermentingController;
import wadosm.breweryhost.logic.fermenting.FermentingService;
import wadosm.breweryhost.logic.general.PowerController;
import wadosm.breweryhost.logic.general.PowerService;
import wadosm.breweryhost.logic.general.UpdateService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class DependenciesTest {

    @TestConfiguration
    static class EmployeeServiceImplTestContextConfiguration {

        @Bean
        public DriverInterface driverInterface() {
            return new FakeDriverInterface();
        }
    }

    @Autowired
    UpdateService updateService;

    @Autowired
    PowerService powerService;

    @Autowired
    PowerController powerController;

    @Autowired
    FermentingService fermentingService;

    @Autowired
    FermentingController fermentingController;

    @Autowired
    BrewingService brewingService;

    @Autowired
    BrewingController brewingController;

    @Test
    void depencencies_check() {
        assertThat(updateService).isNotNull();

        assertThat(powerService).isNotNull();

        assertThat(powerController).isNotNull();

        assertThat(fermentingService).isNotNull();

        assertThat(fermentingController).isNotNull();

        assertThat(brewingService).isNotNull();

        assertThat(brewingController).isNotNull();
    }
}