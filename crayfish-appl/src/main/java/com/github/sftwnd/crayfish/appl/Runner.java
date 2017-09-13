package com.github.sftwnd.crayfish.appl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 *  Загрузчик базового приложения командной строки
 */

@Component
public class Runner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    @Override
    public void run(String... args) throws Exception {
        logger.debug("{}[CommandLineRunner] has been started.", this.getClass().getSimpleName());
    }

}
