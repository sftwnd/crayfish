package com.github.sftwnd.crayfish.amqp.consume.jmx;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ashindarev on 12.09.16.
 */
public class AmqpConsumerStatistics implements AmqpConsumerStatisticsMBean {

    protected final AtomicLong messageReceived         = new AtomicLong(0);
    protected final AtomicLong messageAcknowledged     = new AtomicLong(0);
    protected final AtomicLong messageAcknowledgeError = new AtomicLong(0);
    protected final AtomicLong messageAcknowledgeLost  = new AtomicLong(0);
    protected final AtomicLong messageCompleted        = new AtomicLong(0);

    protected final AtomicLong firstTick = new AtomicLong(0L);
    protected final AtomicLong lastTick = new AtomicLong(0L);

    public long getMessageReceived() {
        return messageReceived.get();
    }

    public long getMessageAcknowledged() {
        return messageAcknowledged.get();
    }

    public long getMessageAcknowledgeError() {
        return messageAcknowledgeError.get();
    }

    public long getMessageAcknowledgeLost() {
        return messageAcknowledgeLost.get();
    }

    public long getMessageCompleted() {
        return messageCompleted.get();
    }

    public Date getFirstMessageDateTime() {
        return new Date(firstTick.get());
    }

    public Date getLastMessageDateTime() {
        return new Date(lastTick.get());
    }

    public void addMessageReceived(long value) {
        messageReceived.addAndGet(value);
    }

    public void addMessageAcknowledged(long value) {
        messageAcknowledged.addAndGet(value);
    }

    public void addMessageAcknowledgeError(long value) {
        messageAcknowledgeError.addAndGet(value);
    }

    public void addMessageAcknowledgeLost(long value) {
        messageAcknowledgeLost.addAndGet(value);
    }

    public void addMessageCompleted(long value) {
        messageCompleted.addAndGet(value);
    }

    public void syncFirstTick() {
        firstTick.compareAndSet(0L, System.currentTimeMillis());
    }

    public void syncLastTick() {
        firstTick.set(System.currentTimeMillis());
    }

}