package org.rookedsysc.openapirequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenApiRequestApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiRequestApplication.class, args);
    }

}
