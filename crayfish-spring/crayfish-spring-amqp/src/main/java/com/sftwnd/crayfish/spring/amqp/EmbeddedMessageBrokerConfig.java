package com.sftwnd.crayfish.spring.amqp;

import com.sftwnd.crayfish.embedded.amqp.qpid.EmbeddedMessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * A resource that starts a Apache Qpid Message Queue Broker.
 *
 */
@Configuration
public class EmbeddedMessageBrokerConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMessageBrokerConfig.class);

    @Value(value = "${qpid.broker_name:}")
    private String brokerName;

    @Value("${qpid.amqp_port:0}")
    private int amqpPort;

    @Value("${qpid.http_port:0}")
    private int httpPort;

    @Value("${qpid.virtual_host:default}")
    private String virtualHost;

    @Value("${qpid.username:guest}")
    private String username;

    @Value("${qpid.password:guest}")
    private String password;

    private Map<String, Object> qpid;
    public Map<String, Object> getQpid() {
        return this.qpid;
    }

    @Bean("embeddedMessageBroker")
    @ConditionalOnMissingBean({EmbeddedMessageBroker.class})
    @Conditional(EmbeddedMessageBrokerCondition.class)
    public EmbeddedMessageBroker getEmbeddedMessageBroker() throws Exception {
        EmbeddedMessageBroker result = new EmbeddedMessageBroker(brokerName, amqpPort, httpPort, virtualHost, username, password, null, null);
        result.startUp();
        return result;
    }

    @ConditionalOnMissingBean({EmbeddedMessageBroker.class})
    public static final class EmbeddedMessageBrokerCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            final String qpidBrokerProperty = "qpid.broker";
            final Environment environment = context.getEnvironment();
            // Для qpid.broker рассматриваются только значения yes, no, true, false, 1, 0, ok
            if (environment.containsProperty(qpidBrokerProperty)) {
                String qpidBrokerValue = environment.getProperty(qpidBrokerProperty);
                if (qpidBrokerValue != null) {
                    qpidBrokerValue = qpidBrokerValue.toLowerCase().trim();
                    if ( "true".equals(qpidBrokerValue) ||
                         "yes".equals(qpidBrokerValue) ||
                         "ok".equals(qpidBrokerValue) ||
                         "1".equals(qpidBrokerValue) ) {
                        return true;
                    } else if ( "false".equals(qpidBrokerValue) ||
                                "no".equals(qpidBrokerValue) ||
                                "0".equals(qpidBrokerValue) ) {
                        return false;
                    }
                }
            }
            for (String profile:context.getEnvironment().getActiveProfiles()) {
                if ("cci-srv-mock-embeded-amqp".equals(profile)) {
                    return true;
                }
            }
            for (String property : new String[] {"qpid.broker.name", "qpid.broker.port"}) {
                if ( context.getEnvironment().containsProperty(property)) {
                    return true;
                }
            }
            return false;
        }

    }

}