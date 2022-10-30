package wadosm.breweryhost.device.system;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@Profile("local")
public class SystemServicesDemo implements SystemServices {

    @Override
    public void doReboot() {
        runAndWait("systemctl reboot");
    }

    @Override
    public void doPowerOff() {
        runAndWait("systemctl poweroff");
    }

    @Override
    public void doRestartBrewery() {
        runAndWait("service brewery-host restart");
    }

    @Override
    public void stopService(String serviceName) {
        runAndWait(String.format("service %s stop", serviceName));
    }

    @Override
    public void startService(String serviceName) {
        runAndWait(String.format("service %s start", serviceName));
    }

    @Override
    public void restartService(String serviceName) {
        runAndWait(String.format("service %s restart", serviceName));
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

    private void runAndWait(String command) {
        log.info("Running command: {}", command);
    }
}
