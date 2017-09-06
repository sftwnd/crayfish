package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.consume.jmx.AmqpConsumerStatistics;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.github.sftwnd.crayfish.amqp.consume.jmx.AmqpConsumerStatisticsMBean;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ashindarev on 01.08.16.
 */
public abstract class DefaultAmqpConsumer<Payload> extends BaseAmqpConsumer<Payload> implements AMQPMessageCompleter {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAmqpConsumer.class);

    private volatile long ackTag     = 0L;
    private volatile long lastTag    = 0L;
    private volatile long ackTick    = 0L;
  //private volatile long incomplete = 0L;

    private volatile List<AtomicBoolean>    messages;
    private volatile AmqpConsumerStatistics statistics;

    private List<AmqpOnAckListener>               amqpOnAckListeners       = null;
    private List<AmqpOnArriveListener<Payload>>   amqpOnArriveListeners    = null;
    private List<AmqpOnCompleteListener<Payload>> amqpOnCompleteListeners  = null;
    private List<AmqpOnExceptionListener>         amqpOnExceptionListeners = null;
    private List<AmqpOnShutdownListener>          amqpOnShutdownListeners  = null;

    public DefaultAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize, List<AmqpOnEventListener> listeners) throws IOException {
        super(connection, queue, prefetchSize, ackSize);
        messages = new LinkedList<>();
        statistics = new AmqpConsumerStatistics();
        if (listeners != null) {
            listeners.forEach(l -> addOnEventListener(l));
        }
        start(DefaultAmqpConsumer.class);
    }

    public DefaultAmqpConsumer(Connection connection, String queue, int prefetchSize, int ackSize) throws IOException {
        this(connection, queue, prefetchSize, ackSize, null);
    }

    @Override
    public synchronized void handleMessage(AMQPMessage<Payload> message) throws IOException {
        arrive(message); // Если сообщение пришло, то сообщаем о прибытии...
        logger.debug("Complete: handleMessage({}) [ackTag:{}, ackTick:{}, lastTag:{}, messages:{}] <[#{}@{}]: {}>", message, ackTag, ackTick, lastTag, messages.size(), getChannel().getChannelNumber(), this.hashCode(), getConsumerTag());
    }

    @Override
    public synchronized void handleException(AMQPMessage<IOException> message) throws IOException {
        arrive(message); // Если сообщение пришло, то сообщаем о прибытии...
        logger.debug("Complete: handleException({}) [ackTag:{}, ackTick:{}, lastTag:{}, messages:{}] <[#{}@{}]: {}>", message, ackTag, ackTick, lastTag, messages.size(), getChannel().getChannelNumber(), this.hashCode(), getConsumerTag(), message.getPayload());
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        logger.debug("ShutdownSignal: [ackTag:{}, ackTick:{}, lastTag:{}, messages:{}] <[#{}@{}]>. {}", ackTag, ackTick, lastTag, messages.size(), getChannel().getChannelNumber(), this.hashCode(), getConsumerTag(), sig);
        if (amqpOnShutdownListeners != null) {
            AMQPMessageTag tag = new AMQPMessageTag(consumerTag, getAckTag());
            amqpOnShutdownListeners.forEach( (lsnr) -> lsnr.shutdownSignal(tag, sig) );
        }
    }

    private void arrive(AMQPMessage<?> message) {
      if (message != null) {
            statistics.syncFirstTick();
            statistics.addMessageReceived(1);
            messages.add(new AtomicBoolean(Boolean.FALSE));
          //incomplete++;
            if (message.getPayload() instanceof IOException) {
                @SuppressWarnings("unchecked")
                AMQPMessage<IOException> exeptedMsg = (AMQPMessage<IOException>) message;
                onException(exeptedMsg);
            } else {
                @SuppressWarnings("unchecked")
                AMQPMessage<Payload> arrivedMsg = (AMQPMessage<Payload>) message;
                onArrive(arrivedMsg);
            }
            statistics.syncLastTick();
            if (messages.size() + lastTag != message.getTag().getValue()) {
                logger.error("Invalid {} structure: [messages.size: {}, lastTag: {}, deliveryTag: {}]. Smthing wrong...", this.getClass().getSimpleName(), messages.size(), lastTag, message.getTag());
            }
      } else {
          statistics.syncFirstTick();
      }
    }

    private volatile AtomicBoolean ackOnRecover = new AtomicBoolean(false);

    @Override
    public void handleRecoverOk(String consumerTag) {
        if (ackOnRecover.get() && getConsumerTag().equals(consumerTag)) {
            try {
                ack(true);
            } catch (IOException ioex) {

            }
        }
    }

    private void ack(boolean touch) throws IOException {
        if ( lastTag > ackTag &&                           // Если есть что подтверждать и
             (touch || lastTag-ackTag >= getAckSize())     // безусловное подтверждение или по превышению лимита
           ) {
            try {
                getChannel().basicAck(lastTag, true);
                statistics.addMessageAcknowledged(lastTag - ackTag);
                ackTick = System.currentTimeMillis();
                this.
                onAck(lastTag);
                ackOnRecover.set(false);
            } catch (Exception ex) {
                ackOnRecover.set(true);
                statistics.addMessageAcknowledgeError(lastTag - ackTag);
                statistics.addMessageAcknowledgeLost(lastTag - ackTag + messages.size());
                lastTag += messages.size();
                messages.clear();
                logger.error("Unable to acknowledge RabbitMQ message, tags[ackd:{},ack:{},rcvd:{},qued:{},lost:{}]. Cause: {}.", ackTag, lastTag, lastTag + messages.size(), messages.size(), messages.size() + lastTag - ackTag, ex.getLocalizedMessage());
            } finally {
                ackTag  = lastTag;
            }
            statistics.syncLastTick();
        }
    }

    private void complete(long tag) throws IOException { // Подтверждение обработки сообщения
      //incomplete--;
        if (tag > lastTag) {
            int id = (int) (tag - lastTag) - 1;
            if (id >= 0) {
                try {
                    if (messages.get(id).compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
                        statistics.addMessageCompleted(1);
                    }
                } catch (Exception ex) {
                    throw ex;
                }
            }
            if (id == 0) {
                while (!messages.isEmpty() && messages.get(0).get()) {
                    messages.remove(0);
                    lastTag++;
                }
            }
            // Если есть подтверждение за границей ackSize - пробуем выполнить ack
            if (lastTag-ackTag >= getAckSize()) {
                onComplete(tag);
                ack(false);
                return;
            }
        }
        onComplete(tag);
    }

    @SuppressWarnings("unchecked")
    public final synchronized void addOnEventListener(AmqpOnEventListener amqpOnEventListener) {
        if (amqpOnEventListener instanceof AmqpOnAckListener) {
            if (amqpOnAckListeners == null) {
                amqpOnAckListeners = new LinkedList<>();
            }
            if (!amqpOnAckListeners.contains(amqpOnEventListener)) {
                amqpOnAckListeners.add((AmqpOnAckListener)amqpOnEventListener);
            }
        }
        if (amqpOnEventListener instanceof AmqpOnArriveListener) {
            if (amqpOnArriveListeners == null) {
                amqpOnArriveListeners = new LinkedList<>();
            }
            if (!amqpOnArriveListeners.contains(amqpOnEventListener)) {
                amqpOnArriveListeners.add((AmqpOnArriveListener)amqpOnEventListener);
            }
        }
        if (amqpOnEventListener instanceof AmqpOnCompleteListener) {
            if (amqpOnCompleteListeners == null) {
                amqpOnCompleteListeners = new LinkedList<>();
            }
            if (!amqpOnCompleteListeners.contains(amqpOnEventListener)) {
                amqpOnCompleteListeners.add((AmqpOnCompleteListener<Payload>) amqpOnEventListener);
            }
        }
        if (amqpOnEventListener instanceof AmqpOnExceptionListener) {
            if (amqpOnExceptionListeners == null) {
                amqpOnExceptionListeners = new LinkedList<>();
            }
            if (!amqpOnExceptionListeners.contains(amqpOnEventListener)) {
                amqpOnExceptionListeners.add((AmqpOnExceptionListener) amqpOnEventListener);
            }
        }
        if (amqpOnEventListener instanceof AmqpOnShutdownListener) {
            if (amqpOnShutdownListeners == null) {
                amqpOnShutdownListeners = new LinkedList<>();
            }
            if (!amqpOnShutdownListeners.contains(amqpOnEventListener)) {
                amqpOnShutdownListeners.add((AmqpOnShutdownListener) amqpOnEventListener);
            }
        }
    }

    // Это конечная точка, в которой можно выплюнуть сообщение наружу
    public void onArrive(AMQPMessage<Payload> message) {
        logger.trace("Message<{},{}> has been arrived", message.getTag(), message.getPayload());
        if (amqpOnArriveListeners != null) {
            amqpOnArriveListeners.forEach(l -> l.onArrive(message));
        }
    }

    // Это конечная точка, в которой можно выплюнуть сообщение, выбросившее ошибку при преобразовании, наружу
    public void onException(AMQPMessage<IOException> message) {
        logger.error("Message<{}:...> has been arrived with exception: {}", message.getTag(), message.getPayload() == null ? null : message.getPayload().getMessage());
        if (amqpOnExceptionListeners != null) {
            amqpOnExceptionListeners.forEach(l -> l.onException(message));
        }
    }

    // Здесь конечная точка процесса подтверждения сообщения. На момент вызова сообщение подтверждено (на клиенте), но ack ещё мог и не пройти.
    public void onComplete(long tag) {
        logger.trace("Message[#{}] has been completed. (Consumer: {})", tag, getConsumerTag());
        if (amqpOnCompleteListeners != null && amqpOnCompleteListeners.size() > 0) {
            AMQPMessageTag messageTag = new AMQPMessageTag(getConsumerTag(), tag);
            amqpOnCompleteListeners.forEach(l -> l.onComplete(messageTag));
        }
    }

    // Здесь конечная точка процесса нотификации AMQP брокера о подтверждении пачки сообщений.
    public void onAck(long tag) {
        logger.trace("Tag[#{}] has been acknowledged. (Consumer: {})", tag, getConsumerTag());
        if (amqpOnAckListeners != null && amqpOnAckListeners.size() > 0) {
            AMQPMessageTag messageTag = new AMQPMessageTag(getConsumerTag(), tag);
            amqpOnAckListeners.forEach(l -> l.onAck(messageTag));
        }
    }

    @Override
    public synchronized void messageComplete(AMQPMessageTag tag) throws IOException {
        if (tag != null) {
          if (getConsumerTag().equals(tag.getKey())) {
                complete(tag.getValue());
            } else {
                logger.error (
                    "Unable to Complete/ACK on messageComplete for the message #{}. Consumer tag is not valid: '{}'", tag.getValue(), tag.getKey()
                );
            }
        } else {
            ack(true);
        }
    }

    @Override
    public AMQPMessageTag tag(String consumerTag, Envelope envelope, AMQP.BasicProperties properties) {
        return new AMQPMessageTag(consumerTag, envelope.getDeliveryTag());
    }

    public AmqpConsumerStatisticsMBean getStatistics() {
        return statistics;
    }

    public long getLastTag() {
        return this.lastTag;
    }

    public long getAckTag() {
        return this.ackTag;
    }

    public long getAckTick() {
        return this.ackTick;
    }

}
