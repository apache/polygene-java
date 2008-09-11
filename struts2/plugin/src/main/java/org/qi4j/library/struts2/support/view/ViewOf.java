package org.qi4j.library.struts2.support.view;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ViewOf {
    Class<?> value();
}
