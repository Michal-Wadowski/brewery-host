package wadosm.breweryhost.logic.general;

public interface ConfigProvider {
    Configuration loadConfiguration();
    void saveConfiguration(Configuration configuration);
}
