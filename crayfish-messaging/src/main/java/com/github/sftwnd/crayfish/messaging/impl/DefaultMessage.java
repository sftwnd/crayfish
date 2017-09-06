package com.github.sftwnd.crayfish.messaging.impl;

import com.github.sftwnd.crayfish.messaging.Message;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Класс, реализующий тэгированное сообщение. Суть в том, что можно прикрутить к сообщению некий Tag, который использовать
 * для его (сообщения) дальнейшей обработки.
 */
public class DefaultMessage<Tag extends Comparable<Tag>, Payload> implements Message<Tag, Payload> {

    private Tag     tag;
    private Payload payload;

    public DefaultMessage(DefaultMessage<Tag, Payload> message) {
        this(message == null ? null : message.getTag(), message == null ? null : message.getPayload());
    }

    public DefaultMessage(Tag tag, Payload payload) {
        assert tag != null;
        assert payload != null;
        this.tag = tag;
        this.payload = payload;
    }

    public Tag getTag() {
        return tag;
    }

    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                           .append("tag", tag)
                           .append("payload", payload)
                            .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() != getClass()) {
            return false;
        } else {
            @SuppressWarnings("unchecked")
            DefaultMessage<Tag, Payload> message = (DefaultMessage)obj;
            return new EqualsBuilder()
                      .append(tag, message.getTag())
                      .append(payload, message.getPayload())
                      .isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                  .append(this.getClass().getCanonicalName())
                  .append(tag)
                  .append(payload)
                  .toHashCode();
    }

}
