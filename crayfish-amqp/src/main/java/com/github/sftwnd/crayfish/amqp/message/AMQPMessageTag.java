package com.github.sftwnd.crayfish.amqp.message;

import javafx.beans.NamedArg;
import javafx.util.Pair;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by ashindarev on 15.09.16.
 */
public class AMQPMessageTag extends Pair<String, Long> implements Comparable<AMQPMessageTag> {

    private static final long serialVersionUID = 173480535312847635L;

    /**
     * Создаётся идентификатор сообщения с привязкой к тэгу канала
     *
     * @param key   Тэг канала
     * @param value Идентификатор сообщения
     */
    public AMQPMessageTag(@NamedArg("key") String key, @NamedArg("value") Long value) {
        super(key, value);
        assert key != null;
        assert value != null;
    }

    public AMQPMessageTag(@NamedArg("tag") AMQPMessageTag tag) {
        this(tag.getKey(), tag.getValue());
    }

    @Override
    public int compareTo(AMQPMessageTag o) {
        int result = 1;
        if (o != null) {
            result = getKey().compareTo(o.getKey());
            if (result == 0) {
                result = getValue().compareTo(o.getValue());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                           .append("consumerTag", getKey())
                           .append("deliveryTag", getValue())
                            .build();
    }

}
