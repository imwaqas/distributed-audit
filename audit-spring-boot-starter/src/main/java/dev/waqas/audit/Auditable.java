package dev.waqas.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audits method completion (success or failure). Messages are SpEL expressions with variables:
 * {@code #args}, {@code #methodName}, and on success {@code #result}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Auditable {

    /**
     * SpEL evaluated after successful return.
     */
    String successMessage();

    /**
     * SpEL evaluated on exception; if blank, a default message is built from {@link #successMessage()} and the exception.
     */
    String failureMessage() default "";

    /**
     * Logical action name; defaults to the method name.
     */
    String action() default "";
}
