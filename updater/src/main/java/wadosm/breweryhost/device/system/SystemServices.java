package wadosm.breweryhost.device.system;

public interface SystemServices {

    void doReboot();

    void doPowerOff();

    void stopService(String serviceName);

    void startService(String serviceName);

    void restartService(String serviceName);

    String getServiceInfo(String serviceName);

    void synchronize();

    void heartBeat(boolean enable);
}
