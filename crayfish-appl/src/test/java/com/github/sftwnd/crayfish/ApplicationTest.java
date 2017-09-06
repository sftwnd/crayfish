package com.github.sftwnd.crayfish;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ashindarev on 10.03.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class ApplicationTest {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void applicationTest() {
        Assert.assertNotNull("applicationContext is null", applicationContext);
        ApplicationTestComponent applicationTestComponent = applicationContext.getBean(ApplicationTestComponent.class, "ApplicationTestComponent");
        Assert.assertNotNull("ApplicationTestComponent does not exists", applicationTestComponent);
        Assert.assertEquals("ApplicationTestComponent class is wrong", ApplicationTestComponent.class, applicationTestComponent.getClass());
    }

    @Component("ApplicationTestComponent")
    public static class ApplicationTestComponent {
    }

}