package wadosm.breweryhost.controller;

import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.filesystem.FilesManager;
import wadosm.breweryhost.system.SystemServices;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UpdateManager {

    private final String UPLOADS_PATH = "/bluetooth";
    private final String CONTROLLER_DESTINATION = "/usr/local/share/brewery-host.jar";

    FilesManager filesManager;

    SystemServices systemServices;

    public UpdateManager(FilesManager filesManager, SystemServices systemServices) {
        this.filesManager = filesManager;
        this.systemServices = systemServices;
    }

    protected void updateBluetoothClients() {

    }

    protected void updateDriverService() {

    }

    protected void updateControllerService(String srcFile) {
        if (!filesManager.isFileAccessible(srcFile)) {
            return;
        }

        if (!filesManager.isFileAccessible(CONTROLLER_DESTINATION)) {
            log.error("Can't write to {}", CONTROLLER_DESTINATION);
            return;
        }

        log.info("Updating from {} to {}", srcFile, CONTROLLER_DESTINATION);

        boolean result = filesManager.moveFile(srcFile, CONTROLLER_DESTINATION);

        log.info("Done: {}", result);

        systemServices.restartService("brewery-host");
    }

    @Scheduled(fixedRate = 1000)
    public void checkUpdates() {
        List<String> files = filesManager.listFiles(UPLOADS_PATH);
        Optional<String> file = getBreweryControllerFile(files);
        file.ifPresent(this::updateControllerService);
    }

    private Optional<String> getBreweryControllerFile(List<String> files) {
        return files.stream().filter(
                x -> x.startsWith(UPLOADS_PATH + "/brewery-host") && x.endsWith(".jar")
        ).findFirst();
    }
}
