package wadosm.breweryhost.integrationtests.logic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.UpdaterApplication;
import wadosm.breweryhost.logic.general.UpdaterUpdateService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes=UpdaterApplication.class)
class UpdaterDependenciesTest {

    @Autowired
    UpdaterUpdateService updaterUpdateService;

    @Test
    void depencencies_check() {
        assertThat(updaterUpdateService).isNotNull();
    }
}