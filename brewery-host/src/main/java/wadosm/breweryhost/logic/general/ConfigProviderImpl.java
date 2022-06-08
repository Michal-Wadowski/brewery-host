package wadosm.breweryhost.logic.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Log4j2
public class ConfigProviderImpl implements ConfigProvider {

    private final ObjectMapper mapper;

    @Getter
    @Value("${calibration.file}")
    private String calibrationFile;

    public ConfigProviderImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Configuration loadConfiguration() {
        Configuration configuration;

        try {
            configuration = mapper.readValue(new File(calibrationFile), Configuration.class);
        } catch (IOException e) {
            configuration = Configuration.builder().build();
        }

        return configuration;
    }

    @Override
    public void saveConfiguration(Configuration configuration) {
        try {
            mapper.writeValue(new File(calibrationFile), configuration);
        } catch (IOException e) {
            log.warn("Cant write to file {}", calibrationFile);
        }
    }
}
