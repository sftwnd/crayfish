package com.github.sftwnd.crayfish.common.concurrent;

import java.time.Instant;

@FunctionalInterface
public interface Revocable {

    void revokeUntil(Instant limit);

}
