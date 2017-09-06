package com.github.sftwnd.crayfish.messaging;

/**
 * Created by ashindarev on 02.08.16.
 */
public interface Message<Tag extends Comparable<Tag>, Payload> {

    Tag getTag();

    Payload getPayload();

}
