package org.qi4j.library.struts2.support.list;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ListOf {
    Class<?> value();
}
