package com.github.sftwnd.crayfish.amqp.publish;

import javafx.beans.NamedArg;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AMQPPublishTag implements Comparable<AMQPPublishTag> {

    private static final long serialVersionUID = 173475648312847635L;

    private String exchangeName;
    private String routingKey;

    /**
     * ...
     *
     * @param exchangeName Имя exchange для отправки сообщения
     * @param routingKey Ключ маршрутизации
     */
    public AMQPPublishTag(@NamedArg("exchangeName") String exchangeName, @NamedArg("routingKey") String routingKey) {
        assert exchangeName != null;
        assert routingKey != null;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public AMQPPublishTag(@NamedArg("tag") AMQPPublishTag tag) {
        this(tag.getExchangeName(), tag.getRoutingKey());
    }

    @Override
    public int compareTo(AMQPPublishTag o) {
        int result = 1;
        if (o != null) {
            result = getExchangeName().compareTo(o.getExchangeName());
            if (result == 0) {
                result = getRoutingKey().compareTo(o.getRoutingKey());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                           .append("exchangeName", getExchangeName())
                           .append("routingKey", getRoutingKey())
                            .build();
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

}