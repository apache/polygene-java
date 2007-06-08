package org.qi4j.test.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;

/*
* Copyright (C) Senselogic 2006, all rights reserved
*/
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
@Documented
@Inherited
public @interface FooAnnotation
{
    String value();
}
