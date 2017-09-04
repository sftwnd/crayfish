package com.sftwnd.crayfish.akka.spring.di;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * An actor producer that lets Spring create the Actor instances.
 */
public class SpringActorProducer implements IndirectActorProducer {

  private static final Logger logger = LoggerFactory.getLogger(SpringActorProducer.class);

  final ApplicationContext applicationContext;
  final String actorBeanName;
  final Object[] args;

  public SpringActorProducer(ApplicationContext applicationContext,
                             String actorBeanName) {

    this.applicationContext = applicationContext;
    this.actorBeanName = actorBeanName;
    this.args = null;
    logger.trace("{} constructor('{}','{}') noargs", SpringActorProducer.class.getSimpleName(), String.valueOf(applicationContext), actorBeanName);
  }
  public SpringActorProducer(ApplicationContext applicationContext,
                             String actorBeanName,
                             Object... args) {
    this.applicationContext = applicationContext;
    this.actorBeanName = actorBeanName;
    this.args = args;
    logger.trace("{} constructor('{}','{}')", SpringActorProducer.class.getSimpleName(), String.valueOf(applicationContext), actorBeanName);
  }

  @Override
  public Actor produce() {
    Actor actor =  (Actor) ( args == null || args.length == 0 ? applicationContext.getBean(actorBeanName) : applicationContext.getBean(actorBeanName, args));
    logger.trace("produce()[actorBeanName:'{}'] -> {}", actorBeanName, String.valueOf(actor));
    return actor;
  }

  @Override
  public Class<? extends Actor> actorClass() {
    @SuppressWarnings("unchecked")
    Class<? extends Actor> clazz = (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    logger.trace("actorClass()[actorBeanName:'{}'] -> {}", actorBeanName, clazz.getCanonicalName());
    return clazz;
  }
}
