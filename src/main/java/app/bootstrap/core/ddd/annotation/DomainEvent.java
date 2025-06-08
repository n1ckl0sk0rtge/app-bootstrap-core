package app.bootstrap.core.ddd.annotation;

import app.bootstrap.core.ddd.AggregateRoot;
import jakarta.annotation.Nonnull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Domain Event in Domain-Driven Design (DDD).
 *
 * <p>A Domain Event represents something significant that occurred in the domain. It is typically
 * used to capture and communicate state changes or important occurrences within an aggregate.
 * This annotation allows the runtime identification and processing of domain events.</p>
 *
 * <p>Domain Events are immutable and represent past occurrences. They are typically dispatched
 * by an {@link AggregateRoot} when its state changes in a way that is significant to the domain.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @DomainEvent(dispatchingAggregate = Order.class, description = "Triggered when an order is placed")
 * public class OrderPlacedEvent {
 *     // Event properties
 * }}
 * </pre>
 *
 * @see AggregateRoot
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainEvent {

    /**
     * Specifies the {@link AggregateRoot} class that is responsible for dispatching this event.
     * This establishes a clear relationship between the event and its source aggregate.
     *
     * @return the Class object representing the aggregate root that dispatches this event
     */

    @Nonnull
    Class<? extends AggregateRoot<?>> dispatchingAggregate();

    /**
     * Provides a descriptive explanation of when and why this domain event occurs.
     * This description helps to document the business meaning of the event.
     *
     * @return a string describing the domain event's purpose and trigger conditions
     */
    @Nonnull
    String description() default "";
}
