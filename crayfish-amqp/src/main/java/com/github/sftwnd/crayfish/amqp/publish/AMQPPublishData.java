package com.github.sftwnd.crayfish.amqp.publish;

import com.rabbitmq.client.AMQP;
import javafx.beans.NamedArg;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AMQPPublishData {

    private static final long serialVersionUID = 173846578902847635L;

    private AMQP.BasicProperties props;
    private byte[] body;
    /**
     * ...
     *
     * @param props Properties отправляемого сообщения
     * @param body  Тело отправляемого сообщения
     */
    public AMQPPublishData(@NamedArg("props") AMQP.BasicProperties props, @NamedArg("body") byte[] body) {
        assert props != null;
        this.props = props;
        this.body = body;
    }

    public AMQPPublishData(@NamedArg("tag") AMQPPublishData tag) {
        this(tag.getProps(), tag.getBody());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                           .append("props", getProps())
                           .append("body", String.valueOf(getBody()))
                            .build();
    }

    public AMQP.BasicProperties getProps() {
        return props;
    }

    public byte[] getBody() {
        return body;
    }

}