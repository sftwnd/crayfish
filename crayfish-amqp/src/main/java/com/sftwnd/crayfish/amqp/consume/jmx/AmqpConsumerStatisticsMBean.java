package com.sftwnd.crayfish.amqp.consume.jmx;

import java.util.Date;

/**
 * Created by ashindarev on 12.09.16.
 */
public interface AmqpConsumerStatisticsMBean {

    long getMessageReceived();
    long getMessageAcknowledged();
    long getMessageAcknowledgeError();
    long getMessageAcknowledgeLost();
    long getMessageCompleted();
    Date getFirstMessageDateTime();
    Date getLastMessageDateTime();

}
