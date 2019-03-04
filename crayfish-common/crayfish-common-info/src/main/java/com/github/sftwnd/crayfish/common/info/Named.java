package com.github.sftwnd.crayfish.common.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nonnull;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Named {

    protected String name;

    @JsonCreator
    public Named(@Nonnull String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
