package com.github.sftwnd.crayfish.distributed.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import com.github.sftwnd.crayfish.distributed.core.instance.InstanceInfo;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class InstanceInfoAddressed extends InstanceInfoCoordinated {

    private static final Logger logger = LoggerFactory.getLogger(InstanceInfo.class);
    private static final MessageSource messageSource         = I18n.getMessageSource();
    private static final String invalidJmxPort               = "crayfish-distributed-core.invalidJmxPort";
    private static final String invalidJmxPortMsg            = "Не удаётся определить порт JMX сервиса. com.sun.management.jmxremote.port={}";
    private static final String unableToIdentifyIPAddress    = "crayfish-distributed-core.unableToIdentifyIPAddress";
    private static final String unableToIdentifyIPAddressMsg = "Не удаётся определить IP-адрес хоста";

    private String address;

    @JsonCreator
    public InstanceInfoAddressed() {
        this(null, null);
    }

    public InstanceInfoAddressed(@Nullable String address) {
        this(address, null);
    }

    public InstanceInfoAddressed(@Nullable String address, @Nullable Instant activationTime) {
        super(activationTime);
        this.address = address;
    }

    public InstanceInfoAddressed(@Nonnull InstanceInfoAddressed info ) {
        this(info.address, info.getActivationTime());
    }

    public static InstanceInfoAddressed construct() {
        return new InstanceInfoAddressed(constructAddress(), Instant.now());
    }

    private static final String constructAddress() {
        StringBuilder sb = new StringBuilder("");
        try {
            sb.append(InetAddress.getLocalHost());
        } catch (UnknownHostException uhex) {
            if (logger.isDebugEnabled()) {
                logger.warn(messageSource.messageDef(unableToIdentifyIPAddress, unableToIdentifyIPAddressMsg));
            }
        }
        try {
            int port = Integer.parseInt(System.getProperty("com.sun.management.jmxremote.port"));
            sb.append(':').append(port);
        } catch (NumberFormatException nfex) {
            if (logger.isDebugEnabled() && nfex.getLocalizedMessage() != null) {
                logger.warn(messageSource.messageDef(invalidJmxPort, invalidJmxPortMsg, System.getProperty("com.sun.management.jmxremote.port")));
            }
        }
        return sb.toString();
    }

    @Override
    public Object clone() {
        return InstanceInfoAddressed.class.equals(this.getClass()) ? new InstanceInfoAddressed(this) : super.clone();
    }

}
