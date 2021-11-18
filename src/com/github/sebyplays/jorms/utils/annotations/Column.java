package com.github.sebyplays.jorms.utils.annotations;

import com.github.sebyplays.jorms.utils.ColumnType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    ColumnType type();
    String name() default "{nameOfField}";
    boolean primaryKey() default false;
}
