package com.github.sftwnd.crayfish.common.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseNamed implements Named {

    @Getter
    @Setter(value = AccessLevel.PROTECTED)
    @NonNull
    private String name;

    @JsonCreator
    public BaseNamed(@Nonnull String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

}
