package wadosm.breweryhost.device.system;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
@Log4j2
@Profile("demo")
public class SystemServicesDemo implements SystemServices {

    @Override
    public void doReboot() {
        runAndWait("sudo systemctl reboot");
    }

    @Override
    public void doPowerOff() {
        runAndWait("sudo systemctl poweroff");
    }

    @Override
    public void stopService(String serviceName) {
        runAndWait(String.format("sudo service %s stop", serviceName));
    }

    @Override
    public void startService(String serviceName) {
        runAndWait(String.format("sudo service %s start", serviceName));
    }

    @Override
    public void restartService(String serviceName) {
        runAndWait(String.format("sudo service %s restart", serviceName));
    }

    @Override
    public String getServiceInfo(String serviceName) {
        log.info("getServiceInfo...");
        return "this is demo application";
    }

    @Override
    public void synchronize() {
        runAndWait("sync");
    }

    @Override
    public void heartBeat(boolean enable) {
        log.info("heartBeat: {}", enable);
    }

    private void runAndWait(String command) {
        log.info("Running command: {}", command);
    }
}
