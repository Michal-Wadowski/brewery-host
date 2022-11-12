package wadosm.breweryhost.logic.general;

import wadosm.breweryhost.logic.general.model.Configuration;

import java.util.function.Function;

public interface ConfigProvider {
    Configuration loadConfiguration();
    void saveConfiguration(Configuration configuration);
    void updateAndSaveConfiguration(Function<Configuration, Configuration> updateConfiguration);
}
