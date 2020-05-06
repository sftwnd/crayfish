package com.github.sftwnd.crayfish.distributed.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import com.github.sftwnd.crayfish.common.json.JsonToStringProcessed;
import com.github.sftwnd.crayfish.distributed.core.resource.ResourceInfo;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class ResourceInfoCoordinated extends JsonToStringProcessed implements ResourceInfo {

    private static final MessageSource messageSource = I18n.getMessageSource();
    private static final String UNSUPPORTED_OPERATION = "crayfish-distributed-core.unsupportedOperation";
    private static final String UNSUPPORTED_OPERATION_MSG = "Операция {} не поддерживается для объекта {}";

    private String  owner;
    private Instant ownTime;
    private Instant suspendTime;

    @JsonCreator
    public ResourceInfoCoordinated() {
        this(null, null);
    }

    public ResourceInfoCoordinated(@Nullable String owner) {
        this(owner, null);
    }

    public ResourceInfoCoordinated(@Nullable String owner, @Nullable Instant ownTime) {
        this(owner, ownTime, null);
    }

    public ResourceInfoCoordinated(@Nullable String owner, @Nullable Instant ownTime, @Nullable Instant suspendTime) {
        this.owner = owner;
        this.ownTime = ownTime;
        this.suspendTime = suspendTime;
    }

    public ResourceInfoCoordinated(@Nonnull ResourceInfoCoordinated info) {
        this(info.getOwner(), info.getOwnTime(), info.getSuspendTime());
    }


    @Override
    @SuppressWarnings("squid:S2975")
    public Object clone() {
        if (ResourceInfoCoordinated.class.equals(this.getClass())) {
            return new ResourceInfoCoordinated(this);
        } else {
            return ExceptionUtils.uncheckExceptions(new UnsupportedOperationException(messageSource.messageDef(UNSUPPORTED_OPERATION, UNSUPPORTED_OPERATION_MSG, this.getClass().getCanonicalName()+"::clone", this)));
        }
    }

}
