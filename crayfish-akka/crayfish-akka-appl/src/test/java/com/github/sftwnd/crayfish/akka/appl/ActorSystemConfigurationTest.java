package com.github.sftwnd.crayfish.akka.appl;

import akka.actor.ActorSystem;
import com.github.sftwnd.crayfish.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ashindarev on 10.03.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class ActorSystemConfigurationTest {

    @Autowired
    @Qualifier(value = "crayfish-actorSystem")
    private ActorSystem actorSystem;

    @Autowired
    @Qualifier("crayfish-actorSystemConfiguration")
    private ActorSystemConfiguration actorSystemConfiguration;

    @Test
    public void actorSystemTest() {
        Assert.assertNotNull("crayfish-actorSystem is not defined", actorSystem != null);
        Assert.assertNotNull("crayfish-actorSystemConfiguration is not defined", actorSystemConfiguration != null);
        Assert.assertEquals("invalid actorSystem name", actorSystemConfiguration.akkaSystemName, actorSystem.name());
    }

}