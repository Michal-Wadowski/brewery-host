package wadosm.breweryhost.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import wadosm.breweryhost.filesystem.FilesManager;
import wadosm.breweryhost.system.SystemServices;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UpdateManager {

    private final String UPLOADS_PATH = "/bluetooth";

    private final String CONTROLLER_DESTINATION = "/usr/local/share/brewery-host.jar";

    private final String DRIVER_DESTINATION = "/usr/local/bin/driver";

    private final String BLUETOOTH_CONFIG_DESTINATION = "/etc/custom-services/bluetooth-pins.cfg";

    FilesManager filesManager;

    SystemServices systemServices;

    public UpdateManager(FilesManager filesManager, SystemServices systemServices) {
        this.filesManager = filesManager;
        this.systemServices = systemServices;
    }

    protected void updateDriverService(String srcFile) {
        systemServices.stopService("driver");

        if (updateService(srcFile, DRIVER_DESTINATION)) {
            cleanDriverFiles();
        }

        systemServices.startService("driver");
    }

    protected void updateControllerService(String srcFile) {
        if (updateService(srcFile, CONTROLLER_DESTINATION)) {
            systemServices.restartService("brewery-host");

            cleanControllerFiles();
        }
    }

    protected void updateBluetoothConfigFile(String srcFile) {
        if (updateBluetoothConfig(srcFile, BLUETOOTH_CONFIG_DESTINATION)) {
            systemServices.restartService("after-bluetooth");

            cleanBluetoothConfigFile();
        }
    }

    private void cleanControllerFiles() {
        List<String> files = getBreweryFiles();
        log.info("Cleanup: {}", files);
        filesManager.deleteFiles(files);
    }

    private void cleanDriverFiles() {
        List<String> files = getDriverFiles();
        log.info("Cleanup: {}", files);
        filesManager.deleteFiles(files);
    }

    private void cleanBluetoothConfigFile() {
        List<String> files = getBluetoothConfigFiles();
        log.info("Cleanup: {}", files);
        filesManager.deleteFiles(files);
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

    @Scheduled(fixedRate = 1000)
    public void checkUpdates() {
        List<String> breweryFiles = getBreweryFiles();
        if (breweryFiles.size() > 0) {
            updateControllerService(breweryFiles.get(0));
        }

        List<String> driverFiles = getDriverFiles();
        if (driverFiles.size() > 0) {
            updateDriverService(driverFiles.get(0));
        }

        List<String> bluetoothConfigFiles = getBluetoothConfigFiles();
        if (bluetoothConfigFiles.size() > 0) {
            updateBluetoothConfigFile(bluetoothConfigFiles.get(0));
        }
    }

    private List<String> getBreweryFiles() {
        return filesManager.listFiles(UPLOADS_PATH, "brewery-host", ".jar");
    }

    private List<String> getDriverFiles() {
        return filesManager.listFiles(UPLOADS_PATH, "driver");
    }

    private List<String> getBluetoothConfigFiles() {
        return filesManager.listFiles(UPLOADS_PATH, "bluetooth-pins.cfg");
    }
}
