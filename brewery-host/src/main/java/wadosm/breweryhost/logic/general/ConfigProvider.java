package wadosm.breweryhost.logic.general;

import wadosm.breweryhost.logic.general.model.Configuration;

public interface ConfigProvider {
    Configuration loadConfiguration();
    void saveConfiguration(Configuration configuration);
}
