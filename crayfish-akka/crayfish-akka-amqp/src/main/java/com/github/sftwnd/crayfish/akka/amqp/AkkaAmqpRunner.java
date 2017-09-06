package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.github.sftwnd.crayfish.akka.spring.di.SpringExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *  Загрузчик базового приложения командной строки
 */

@Component("crayfish-AkkaAmqpRunner")
@Profile("crayfish-akka-amqp")
@DependsOn(value = {"crayfish-spring-amqp", "crayfish-actorSystem"})
public class AkkaAmqpRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AkkaAmqpRunner.class);

    @Autowired
    ActorSystem actorSystem;

    @Autowired
    ApplicationContext applicationContext;

    @Value("${com.github.sftwnd.crayfish.akka.amqp.actor-name:crayfish-amqp}")
    private volatile String amqpActorName;

    @Bean(name="crayfish-amqpActor")
    ActorRef getAmqpActor() {
        return actorSystem.actorOf (
                SpringExtension.SpringExtProvider.get(actorSystem).props("crayfish-AmqpActor"), amqpActorName
        );
    }

    @Override
    public void run(String... args) throws Exception {
        ActorRef amqpActor = applicationContext.getBean("crayfish-amqpActor", ActorRef.class);
        logger.debug("Crayfish {}[{}] has been started as [{}]", AkkaAmqpRunner.class.getSimpleName(), amqpActorName, amqpActor.path());
    }

}
