package wadosm.breweryhost.device.system;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class SystemServicesImpl implements SystemServices {

    @Override
    public void doReboot() {
        runAndWait("sudo systemctl restart");
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
        try {
            Process process = Runtime.getRuntime().exec(String.format("sudo service %s status", serviceName));
            process.waitFor();
            return new String(process.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void synchronize() {
        runAndWait("sync");
    }

    private void runAndWait(String command) {
        try {
            log.info("Running command: {}", command);
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
