package com.sftwnd.crayfish.embedded.derby;

import org.apache.derby.drda.NetworkServerControl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * Created by ashindarev on 23.02.17.
 */
@RunWith(SpringRunner.class)
@PropertySource("classpath:application.properties")
@SpringBootTest//(classes = {Application.class, EmbededDerbyServer.class})
@ActiveProfiles({"crayfish-embeded-derby-server"})
@AutoConfigurationPackage
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public class EmbededDerbyServerTest {

    @Autowired
    private NetworkServerControl networkServerControl;

    @Test
    public void getDerbyServer() throws Exception {
        String port = new String(networkServerControl.getCurrentProperties().getProperty("derby.drda.portNumber"));
        try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:"+port+"/derbydb;create=true")) {
            try (ResultSet rset = conn.prepareStatement("SELECT COUNT(1) FROM SYS.SYSTABLES").executeQuery()) {
                Assert.assertTrue("Check derby connection", rset.next());
            }
        }
    }

}