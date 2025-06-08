package app.bootstrap.core.ddd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an Aggregate Root in Domain-Driven Design (DDD).
 *
 * <p>An Aggregate Root is a domain pattern that encapsulates and controls access to a cluster
 * of domain objects (the aggregate). It serves as the main entry point to the aggregate and
 * ensures the consistency of changes to the objects within its boundaries.</p>
 *
 * <p>Key characteristics of an Aggregate Root:</p>
 * <ul>
 *   <li>It is the only entry point for modifications to objects within the aggregate</li>
 *   <li>It enforces invariants and maintains consistency for the entire aggregate</li>
 *   <li>External objects may only hold references to the aggregate root, not its internal members</li>
 *   <li>It is responsible for ensuring that all entities and value objects within its boundary
 *       remain in a consistent state</li>
 * </ul>
 *
 * <p>This annotation should be applied to classes that serve as the root entity of an aggregate
 * in your domain model.</p>
 *
 * @see Entity
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Entity
public @interface AggregateRoot {

    String description() default "";
}
