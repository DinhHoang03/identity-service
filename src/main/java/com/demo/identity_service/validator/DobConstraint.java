package com.demo.identity_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//Tạo ra một custom annotation với 3 phương thức bắt buộc chính và một phương thức điều kiện nhập vào
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {DobValidator.class})
public @interface DobConstraint {
    //3 properties cơ bản của một custom annotation validation
    String message() default "Invalid date of birth";

    int min();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
