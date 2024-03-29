package wadosm.breweryhost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BreweryHostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BreweryHostApplication.class, args);
    }

}
