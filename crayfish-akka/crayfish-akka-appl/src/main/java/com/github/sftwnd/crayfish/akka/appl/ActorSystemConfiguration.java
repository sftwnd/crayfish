package com.github.sftwnd.crayfish.akka.appl;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.github.sftwnd.crayfish.akka.spring.di.SpringExtension;

/**
 * Данная конфигурация создаёт Actor System как Spring-овый bean
 */
@Configuration("crayfish-actorSystemConfiguration")
@Profile("crayfish-akka-appl")
public class ActorSystemConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ActorSystemConfiguration.class);

    @Autowired
    ApplicationContext applicationContext;

    @Value("${com.github.sftwnd.crayfish.akka.system-name:crayfish-system}")
    public String akkaSystemName;

    @Bean(name = {"crayfish-actorSystem"})
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = null;
        boolean finded = false;
        try {
            if (applicationContext.containsBean(akkaSystemName)) {
                finded = true;
                actorSystem = applicationContext.getBean(akkaSystemName, ActorSystem.class);
            } else {
                actorSystem = ActorSystem.create(akkaSystemName, applicationContext.getBean("crayfish-akkaConfiguration", Config.class));
                // initialize the application context in the Akka Spring Extension
                SpringExtension.SpringExtProvider.get(actorSystem).initialize(applicationContext);
            }
        } catch (Exception ex) {
            logger.error("Unable to construct @Bean[crayfish-actorSystem:{}]", akkaSystemName, ex);
        } finally {
            if (actorSystem != null) {
                logger.debug("@{}Bean[crayfish-actorSystem] -> actorSystem[{}]", finded ? "Find:" : "", actorSystem.toString());
            }
        }
        return actorSystem;
    }

    @Value("${akka.config:akka.conf}")
    public String akkaConfigFile;

    @Bean(name = {"crayfish-akkaConfiguration"})
    public Config akkaConfiguration() {
        logger.debug("@Bean[crayfish-akkaConfiguration] -> akkaConfiguration([ConfigFactory.load({})])", akkaConfigFile == null ? "" : akkaConfigFile);
        return akkaConfigFile == null ? ConfigFactory.load() : ConfigFactory.load(akkaConfigFile);
    }

}
