package wadosm.breweryhost.integrationtests.logic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wadosm.breweryhost.UpdaterApplication;
import wadosm.breweryhost.device.filesystem.FilesManager;
import wadosm.breweryhost.device.system.SystemServices;
import wadosm.breweryhost.logic.general.UpdateService;
import wadosm.breweryhost.logic.general.UpdaterUpdateService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UpdaterApplication.class)
class UpdaterDependenciesTest {

    @Autowired
    UpdateService updateService;

    @Autowired
    FilesManager filesManager;

    @Autowired
    SystemServices systemServices;

    @Test
    void depencencies_check() {
        assertThat(updateService).isNotNull();
    }

    @Test
    void updateSystem() {
        UpdaterUpdateService updaterUpdateService = new UpdaterUpdateService(filesManager, systemServices);

        updaterUpdateService.updateSystem();
    }
}