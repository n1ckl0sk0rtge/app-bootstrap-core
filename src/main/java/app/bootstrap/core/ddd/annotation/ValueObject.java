package app.bootstrap.core.ddd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Value Object in Domain-Driven Design (DDD).
 *
 * A Value Object is an immutable object that describes some characteristic or attribute
 * but has no conceptual identity. Value Objects are equality comparable based on their
 * attributes rather than identity. They are typically implemented as immutable objects
 * to ensure thread-safety and maintain integrity.
 *
 * Key characteristics of a Value Object:
 * - Immutability: Once created, its state cannot be changed
 * - No identity: Equality is determined by comparing all attributes
 * - Self-contained: Contains all necessary validation rules
 * - Interchangeable: Two Value Objects with the same attributes are considered equal
 *
 * Example usage:
 * {@code
 * @ValueObject
 * public class Money {
 *     private final BigDecimal amount;
 *     private final Currency currency;
 *     // ... constructors and methods
 * }}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValueObject {

    String description() default "";
}
