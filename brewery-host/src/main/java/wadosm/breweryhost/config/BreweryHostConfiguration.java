package wadosm.breweryhost.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import wadosm.breweryhost.DigiPort;
import wadosm.breweryhost.DigiPortImpl;
import wadosm.breweryhost.DriverLoader;
import wadosm.breweryhost.DriverLoaderImpl;

@Configuration
public class BreweryHostConfiguration {

    @Bean
    public DriverLoader driverLoader(Environment environment) {
        return new DriverLoaderImpl(environment);
    }

    @Bean
    @Profile("!debug-driver")
    public DigiPort digiPort(DriverLoader driverLoader) {
        return new DigiPortImpl(driverLoader);
    }

    @Bean
    @Profile("debug-driver")
    public DigiPort digiPortDebug(DriverLoader driverLoader) {
        DigiPortImpl digiPort = new DigiPortImpl(driverLoader);
        digiPort.debugEnable(true);
        return digiPort;
    }

}
