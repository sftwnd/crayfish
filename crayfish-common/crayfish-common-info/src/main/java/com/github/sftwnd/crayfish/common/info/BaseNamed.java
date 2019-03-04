package com.github.sftwnd.crayfish.common.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nonnull;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseNamed implements Named {

    protected String name;

    @JsonCreator
    public BaseNamed(@Nonnull String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
