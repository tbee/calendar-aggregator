package org.tbee.jakarta.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UrlValidatorImpl.class)
@Documented
public @interface UrlValidator {
    String message() default "{org.tbee.jakarta.validator.UrlValidator}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}