package com.github.sftwnd.crayfish.distributed.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
@Slf4j
public class InstanceInfoAddressed extends InstanceInfoCoordinated {

    private static final MessageSource messageSource = I18n.getMessageSource();
    private static final String INVALID_JMX_PORT = "crayfish-distributed-core.invalidJmxPort";
    private static final String INVALID_JMX_PORT_MSG = "Не удаётся определить порт JMX сервиса. com.sun.management.jmxremote.port={}";
    private static final String UNABLE_TO_IDENTIFY_IP_ADDRESS = "crayfish-distributed-core.unableToIdentifyIPAddress";
    private static final String UNABLE_TO_IDENTIFY_IP_ADDRESS_MSG = "Не удаётся определить IP-адрес хоста";

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
                logger.warn(messageSource.messageDef(UNABLE_TO_IDENTIFY_IP_ADDRESS, UNABLE_TO_IDENTIFY_IP_ADDRESS_MSG));
            }
        }
        try {
            int port = Integer.parseInt(System.getProperty("com.sun.management.jmxremote.port"));
            sb.append(':').append(port);
        } catch (NumberFormatException nfex) {
            if (logger.isDebugEnabled() && nfex.getLocalizedMessage() != null) {
                logger.warn(messageSource.messageDef(INVALID_JMX_PORT, INVALID_JMX_PORT_MSG, System.getProperty("com.sun.management.jmxremote.port")));
            }
        }
        return sb.toString();
    }

    @Override
    @SuppressWarnings("squid:S2975")
    public Object clone() {
        return InstanceInfoAddressed.class.equals(this.getClass()) ? new InstanceInfoAddressed(this) : super.clone();
    }

}
