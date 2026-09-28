package com.skillswap.config;

import com.skillswap.service.DataSeederService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeederRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederRunner.class);

    private final DataSeederService dataSeederService;

    public DataSeederRunner(DataSeederService dataSeederService) {
        this.dataSeederService = dataSeederService;
    }

    @Override
    public void run(String... args) {
        log.info("app.seed.enabled=true detected. Executing automatic demo data seeding...");
        dataSeederService.seedDemoData(false);
    }
}
