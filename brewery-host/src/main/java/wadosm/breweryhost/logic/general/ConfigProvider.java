package wadosm.breweryhost.logic.general;

import java.util.List;

public interface ConfigProvider {
    Configuration getConfiguration();
    void setConfiguration(Configuration configuration);

    List<Float> getTemperatureCalibrationOf(String brewingTemperatureSensor);
}
