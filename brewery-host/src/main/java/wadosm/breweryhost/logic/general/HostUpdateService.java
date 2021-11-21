package wadosm.breweryhost.logic.general;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.device.filesystem.FilesManager;
import wadosm.breweryhost.device.system.SystemServices;

import java.util.List;

@Service
@Log4j2
// TODO: Implement application config updates
public class HostUpdateService implements UpdateService {

    private final String UPLOADS_PATH = "/bluetooth";

    private final String UPDATER_DESTINATION = "/usr/local/share/updater.jar";

    FilesManager filesManager;

    SystemServices systemServices;

    public HostUpdateService(FilesManager filesManager, SystemServices systemServices) {
        this.filesManager = filesManager;
        this.systemServices = systemServices;
    }

    protected void updateUpdaterService(String srcFile) {
        if (updateService(srcFile, UPDATER_DESTINATION)) {
            systemServices.restartService("updater");

            cleanUpdaterFiles();
        }
    }

    private void cleanUpdaterFiles() {
        List<String> files = getUpdaterFiles();
        log.info("Cleanup: {}", files);
        filesManager.deleteFiles(files);

        systemServices.synchronize();
    }

    private boolean updateService(String srcFile, String dstFile) {
        if (!filesManager.isFileAccessible(srcFile)) {
            return false;
        }

        if (!filesManager.isFileAccessible(dstFile)) {
            log.error("Can't write to {}", dstFile);

            filesManager.deleteFile(srcFile);
            return false;
        }

        if (filesManager.isFileChecksumValid(srcFile)) {
            log.info("Updating from {} to {}", srcFile, dstFile);

            boolean result = filesManager.copyFile(srcFile, dstFile);

            log.info("Done: {}", result);
            return true;
        }

        return false;
    }

    private boolean updateBluetoothConfig(String srcFile, String dstFile) {
        if (!filesManager.isFileAccessible(srcFile)) {
            return false;
        }

        if (!filesManager.isFileAccessible(dstFile)) {
            log.error("Can't write to {}", dstFile);

            filesManager.deleteFile(srcFile);
            return false;
        }

        if (checkBluetoothConfigFile(srcFile)) {
            log.info("Updating from {} to {}", srcFile, dstFile);

            boolean result = filesManager.copyFile(srcFile, dstFile);

            log.info("Done: {}", result);
            return true;
        }

        return false;
    }

    private boolean checkBluetoothConfigFile(String srcFile) {
        byte[] content = filesManager.readFile(srcFile);
        return new String(content).endsWith("\n\n");
    }

    @Scheduled(fixedRateString = "${update.checkingPeriod}")
    public void checkUpdates() {
        List<String> updaterFiles = getUpdaterFiles();
        if (updaterFiles.size() > 0) {
            updateUpdaterService(updaterFiles.get(0));
        }
    }

    private List<String> getUpdaterFiles() {
        return filesManager.listFiles(UPLOADS_PATH, "updater", ".jar");
    }

}
