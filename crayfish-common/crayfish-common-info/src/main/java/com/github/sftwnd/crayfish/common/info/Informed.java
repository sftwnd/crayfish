package com.github.sftwnd.crayfish.common.info;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Informed<I> {

    I getInfo();

    void setInfo(I info);

}
