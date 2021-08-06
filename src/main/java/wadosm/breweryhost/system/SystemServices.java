package wadosm.breweryhost.system;

public interface SystemServices {

    void doReboot();

    void doPowerOff();

    void stopService(String serviceName);

    void startService(String serviceName);

    void restartService(String serviceName);

    String getServiceInfo(String serviceName);

}
