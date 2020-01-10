package com.github.sftwnd.crayfish.distributed.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import com.github.sftwnd.crayfish.common.json.JsonToStringProcessed;
import com.github.sftwnd.crayfish.distributed.core.instance.InstanceInfo;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class InstanceInfoCoordinated extends JsonToStringProcessed implements InstanceInfo {

    private static final MessageSource messageSource = I18n.getMessageSource();
    private static final String UNSUPPORTED_OPERATION = "crayfish-distributed-core.unsupportedOperation";
    private static final String UNSUPPORTED_OPERATION_MSG = "Операция {} не поддерживается для объекта {}";

    private       Instant      activationTime;
    private final List<String> supportedResourceTypes = new ArrayList<>();

    @JsonCreator
    public InstanceInfoCoordinated() {
        this((Instant)null);
    }


    public InstanceInfoCoordinated(@Nullable Instant activationTime) {
        this.activationTime = activationTime;
    }

    public InstanceInfoCoordinated(@Nonnull InstanceInfoCoordinated info ) {
        this(info.getActivationTime());
        this.supportedResourceTypes.addAll(info.getSupportedResourceTypes());
    }

    public static InstanceInfoCoordinated construct() {
        return new InstanceInfoCoordinated(Instant.now());
    }

    @Override
    @SuppressWarnings("squid:S2975")
    public Object clone() {
        return InstanceInfoCoordinated.class.equals(this.getClass())
               ? new InstanceInfoCoordinated(this)
               : ExceptionUtils.uncheckExceptions(
                       new UnsupportedOperationException(messageSource.messageDef(UNSUPPORTED_OPERATION, UNSUPPORTED_OPERATION_MSG, this.getClass().getCanonicalName()+"::close", this))
                 );
    }

}
