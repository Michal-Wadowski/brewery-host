package wadosm.breweryhost.integrationtests.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wadosm.breweryhost.controller.brewing.BrewingController;
import wadosm.breweryhost.controller.fermenter.FermentingController;
import wadosm.breweryhost.controller.general.PowerController;
import wadosm.breweryhost.controller.general.UpdateService;
import wadosm.breweryhost.driver.DriverInterface;
import wadosm.breweryhost.driver.FakeDriverInterface;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ServicesTest {

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
    PowerController powerController;

    @Autowired
    FermentingController fermentingController;

    @Autowired
    BrewingController brewingController;

    @Test
    void depencencies_check() {
        assertThat(updateService).isNotNull();

        assertThat(powerController).isNotNull();

        assertThat(fermentingController).isNotNull();

        assertThat(brewingController).isNotNull();
    }
}