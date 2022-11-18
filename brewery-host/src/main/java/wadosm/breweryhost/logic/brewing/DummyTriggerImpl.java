package wadosm.breweryhost.logic.brewing;

import lombok.RequiredArgsConstructor;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;
import wadosm.breweryhost.logic.general.ConfigProvider;

class DummyTriggerImpl implements TemperatureThresholdTrigger {

    @Override
    public boolean isTriggered(Double currentTemperature) {
        return true;
    }
}
