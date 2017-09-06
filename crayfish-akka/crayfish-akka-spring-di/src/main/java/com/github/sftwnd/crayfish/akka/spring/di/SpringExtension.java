package com.github.sftwnd.crayfish.akka.spring.di;

import akka.actor.AbstractExtensionId;
import akka.actor.Actor;
import akka.actor.ActorContext;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * An Akka Extension to provide access to Spring managed Actor Beans.
 */
public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt> {

    /**
     * The identifier used to access the SpringExtension.
     */
    public static SpringExtension SpringExtProvider = new SpringExtension();

    private Map<ActorSystem, Map<String, ActorContext>> registredActorContext = new HashMap<>();

    /**
     * Временный хак по сохранению ссылки на актор в контекста Extension.
     * В дальнейшем, возможно, совсем уйдёт.
     * P.S.> TODO: необходимо добавить методы очистки
     */
    public void registerActor(String name, Actor actor) {
        synchronized (registredActorContext) {
            ActorSystem system = actor.context().system();
            if (!registredActorContext.containsKey(system)) {
                registredActorContext.put(system, new HashMap<>());
            }
            registredActorContext.get(system).put(name, actor.context());
        }
    }

    public ActorContext getContext(ActorSystem system, String name) {
        synchronized (registredActorContext) {
            return registredActorContext.containsKey(system)
                    ? registredActorContext.get(system).get(name)
                    : null;
        }
    }

    // TODO: Протестировать. Ни разу не запускал...
    public void unregisterActor(Actor actor) {
        synchronized (registredActorContext) {
            ActorSystem system = actor.context().system();
            for (Map.Entry<String, ActorContext> entry : registredActorContext.get(system).entrySet()) {
                if (entry.getValue() == actor) {
                    // Удаляем Актор-а по совпадению ссылки
                    registredActorContext.get(system).remove(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void unregisterActor(ActorSystem actorSystem, String name) {
        synchronized (registredActorContext) {
            if (registredActorContext.containsKey(actorSystem)) {
                registredActorContext.get(actorSystem).remove(name);
            }
        }
    }



    /**
     * Is used by Akka to instantiate the Extension identified by this
     * ExtensionId, internal use only.
     */
    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    /**
     * The Extension implementation.
     */
    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        /**
         * Used to initialize the Spring application context for the extension.
         * @param applicationContext
         */
        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        /**
         * Create a Props for the specified actorBeanName using the
         * SpringActorProducer class.
         *
         * @param actorBeanName  The name of the actor bean to create Props for
         * @return a Props that will create the named actor bean using Spring
         */
        public Props props(String actorBeanName, Object... args) {
            return args == null
                 ? Props.create(SpringActorProducer.class, applicationContext, actorBeanName)
                 : Props.create(SpringActorProducer.class, applicationContext, actorBeanName, args);
        }
    }
}
