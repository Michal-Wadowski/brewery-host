package wadosm.breweryhost.logic.brewing;

import org.springframework.stereotype.Component;
import wadosm.breweryhost.logic.brewing.model.BrewingSettings;

@Component
public class BrewingSettingsProvider {

    private final BrewingSettings brewingSettings = new BrewingSettings();

    public BrewingSettings getBrewingSettings() {
        return brewingSettings;
    }
}
