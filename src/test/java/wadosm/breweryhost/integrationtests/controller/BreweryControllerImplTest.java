package wadosm.breweryhost.integrationtests.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.controller.BreweryController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BreweryControllerImplTest {

    @Autowired
    BreweryController breweryController;

    @Test
    void depencencies_check() {
        assertThat(breweryController).isNotNull();
    }
}