/*
 * Copyright (c) 2017-20xx Andrey D. Shindarev (ashindarev@gmail.com)
 * This program is made available under the terms of the BSD 3-Clause License.
 */
package com.github.sftwnd.crayfish.common.concurrent;

import java.time.Instant;

@FunctionalInterface
public interface Revocable {

    void revokeUntil(Instant limit);

}
