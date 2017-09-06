package com.github.sftwnd.crayfish.amqp.consume;

import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.rabbitmq.client.ShutdownSignalException;

public interface AmqpOnShutdownListener extends AmqpOnEventListener {

    // Информирует о разрыве соединения/рассоединении с AMQP брокером с передачей последнего успешно подтверждённого сообщения
    void shutdownSignal(AMQPMessageTag tag, ShutdownSignalException sig);

}
