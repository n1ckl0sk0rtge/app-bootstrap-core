package app.bootstrap.core.ddd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Domain-Driven Design (DDD) Entity.
 *
 * In DDD, an Entity is a domain object defined by its identity rather than its attributes.
 * Entities have a unique identifier and can change their attributes while maintaining the same identity.
 * Two entities with the same identifier are considered the same entity, even if they have different attributes.
 *
 * Usage example:
 * {@code
 * @Entity(description = "Represents a customer in the system")
 * public class Customer {
 *     private CustomerId id;
 *     private String name;
 *     // ... other attributes and methods
 * }}
 *
 * @see <a href="https://martinfowler.com/bliki/EvansClassification.html">DDD Entity Pattern</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    String description() default "";
}
